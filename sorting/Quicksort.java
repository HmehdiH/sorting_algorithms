package sorting;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Quicksort<E> extends SortingAlgorithm<E, Comparator<? super E>> {
    QuicksortPivotSelector selector;
    InsertionSort<E> insertionSort;

    public Quicksort(Comparator<? super E> comparator, QuicksortPivotSelector selector) {
        super(comparator);
        this.selector = selector;
        insertionSort = new InsertionSort<>(comparator);
    }

    // Notes for the below methods:
    // * Use the class attribute `comparator` to perform comparisons.
    // * You can use `swap(list, i, j)` to swap indices i and j.
    // * If the list range is given by `from` and `to`, the last element is `list.get(to - 1)`.

    // Sort the given range of the list in-place.
    public void sort(List<E> list, int from, int to) {
        // Base case

        int size = to - from;
        if (size == 0)
            return;
        //use hi lo


        int pivotIndex = partition(list, from, to);

        sort(list, from, pivotIndex);
        sort(list, pivotIndex+1, to);
    }

    // Partition the given range of a list.
    // Return the final position of the pivot.
    private int partition(List<E> list, int from, int to) {
        // The index of the element that should be used as the pivot.
        int pivotIndex = selector.pivotIndex(list, from, to, comparator);

        swap(list, from, pivotIndex);
        pivotIndex = from;
        int lo = from + 1;
        int hi = to - 1;
        E pivot = list.get(pivotIndex);

        while (true) {

            while (lo <= hi && (comparator.compare(list.get(lo), pivot) < 0)) {
                lo++;
            }


            while (lo <= hi && (comparator.compare(list.get(hi), pivot) > 0)) {
                hi--;
            }

            if (lo > hi) {
                break;
            }
            swap(list, lo, hi);
            lo++;
            hi--;


        }

        swap(list, pivotIndex, hi);   // Finally, move the pivot into place
        return hi;
    }

    // Run your own tests here!
    public static void main(String[] args) {
        Quicksort<String> algorithm = new Quicksort<>(Comparator.naturalOrder(), QuicksortPivotSelector.MEDIAN_OF_THREE);

        List<String> list = Arrays.asList("turtle", "cat", "zebra", "swan", "dog");
        algorithm.sort(list);
        System.out.println(list);

        list = Arrays.asList("turtle", "cat", "zebra", "swan", "dog", "horse", "fly", "elephant", "duck", "monkey");
        algorithm.sort(list);
        System.out.println(list);
    }
}
