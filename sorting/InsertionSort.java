package sorting;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class InsertionSort<E> extends SortingAlgorithm<E, Comparator<? super E>> {
    public InsertionSort(Comparator<? super E> comparator) {
        super(comparator);
    }

    // Sort the given list in-place in the range [from, to] (last index is to-1).
    // You should implement the in-place version of insertion sort.
    //
    // Notes:
    // * Use the class attribute `comparator` to perform comparisons.
    // * Recall that list has methods `get` and `set` to access elements.
    // * You can use `swap(list, i, j)` to swap indices i and j.
    // * The last element of a list range given by `from` and `to` is `list.get(to - 1)`.
    // - Make no recursive calls.
    public void sort(List<E> list, int from, int to) {

        for(int i = from + 1; i < to; i++) {
            boolean done = false;
            int j = i - 1;

            while (j >= 0 && !done) {
                int x = comparator.compare(list.get(j+1), list.get(j));
                if (x<0) {
                    swap(list, j+1, j);
                } else {
                    done = true;
                }
                j--;

            }
        }
    }

    // Run your own tests here!
    public static void main(String[] args) {
        InsertionSort<String> algorithm = new InsertionSort<>(Comparator.naturalOrder());

        List<String> list = Arrays.asList("turtle", "cat", "zebra", "swan", "dog");
        algorithm.sort(list);
        System.out.println(list);

        list = Arrays.asList("turtle", "cat", "zebra", "swan", "dog", "horse", "fly", "elephant", "duck", "monkey");
        algorithm.sort(list);
        System.out.println(list);
    }
}
