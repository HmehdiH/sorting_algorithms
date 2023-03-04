package sorting;

import java.util.Arrays;
import java.util.List;

public class MultiKeyQuicksort<E> extends SortingAlgorithm<E, LexicographicComparator<? super E>> {
    QuicksortPivotSelector selector;

    public MultiKeyQuicksort(LexicographicComparator<? super E> comparator, QuicksortPivotSelector selector) {
        super(comparator);
        this.selector = selector;
    }

    public void sort(List<E> list, int from, int to) {
        sort(list, from, to, 0);
    }

    // Sort the given range of the list in-place using *multi-key quicksort*.
    // Multi-key quicksort is a version of quicksort well-adapted to sorting strings.
    // Instead of a partition into two parts, we do a partition into *three* parts:
    // * smaller than the pivot,
    // * equal to the pivot (including the chosen pivot),
    // * larger than the pivot.
    // Afterwards, we continue sorting the smaller and larger parts with the same position.

    // In addition, we are not comparing all the elements fully, but only up to / at some position.
    // For example, when comparing two strings, this would mean that we only compare the characters at index 3.
    // This is much quicker than comparing the entire strings.
    // It also makes the "equal" partition larger (it may contain strings that differ from the pivot at some later position).
    
    // In the recursion, we make sure to continue sorting with the same position in the smaller and larger parts of the partition.
    // However, for the "equal" part of the partition, we continue with position increased by one.
    // That way, we eventually compare all the characters of two strings until we discover a difference.

    // Notes:
    // * The class attribute `comparator` is now a LexicographicComparator.
    //   You should only call its method `comparator.compare(x, y, position)`.
    //   This compares x and y at the specified position.
    //   See LexicographicComparator and CharSequenceComparator for examples of what "position" can mean.
    // * You can use `swap(list, i, j)` to swap indices i and j.
    // * The last element of a list range given by `from` and `to` is `list.get(to - 1)`.
    public void sort(List<E> list, int from, int to, int position) {
        int size = to - from;
        if (size <= 1)
            return;

        int pivotIndex = selector.pivotIndex(list, from, to, comparator);

        swap(list, from, pivotIndex);
        pivotIndex = from;

        int middleHigh = to-1;
        int middleLow = from + 1;
        int i = from + 1;

        while(i <= middleHigh) {
            int result = comparator.compare(list.get(i), list.get(pivotIndex), position);
            if (result > 0){
                //i är mindre än pivot, ska till högra sidan
                swap(list, i, middleHigh);
                middleHigh--;
            } else if(result < 0) {
                //i är mindre än påvot, ska till vänstra sidan
                swap(list, i, middleLow);
                middleLow++;
                i++;
            } else {
                //Lika med pivot (på position x)
                i++;
            }
        }

        swap(list, pivotIndex, middleLow-1);

        if (middleLow > middleHigh) {
            sort(list, from, middleHigh+1, position);         //Strictly lower than pivot
            sort(list, middleLow, to, position);        //Strictly greater than pivot
        } else if (middleLow <= middleHigh) {
            sort(list, middleLow-1, middleHigh+1, position + 1);//Equal to pivot
            sort(list, from, middleLow-1, position);         //Strictly lower than pivot
            sort(list, middleHigh+1, to, position);        //Strictly greater than pivot
        }



    }

    // Run your own tests here!
    public static void main(String[] args) {
        MultiKeyQuicksort<String> algorithm = new MultiKeyQuicksort<>(new CharSequenceComparator(), QuicksortPivotSelector.FIRST);

        List<String> list = Arrays.asList("turtle", "cat", "zebra", "swan", "dog");
        algorithm.sort(list);
        System.out.println(list);

       list = Arrays.asList("turtle", "cat", "zebra", "swan", "dog", "horse", "fly", "elephant", "duck", "monkey");
        algorithm.sort(list);
        System.out.println(list);

        list = Arrays.asList(
                "ab", "aa");
        algorithm.sort(list);
        System.out.println(list);
    }
}
