package sorting;

import java.util.Comparator;
import java.util.List;

import util.Util;

public abstract class SortingAlgorithm<E, C extends Comparator<? super E>> {
    C comparator;

    public SortingAlgorithm(C comparator) {
        this.comparator = comparator;
    }

    public C getComparator() {
        return comparator;
    }

    // Sort a list (using the stored comparator).
    // Implemented using the method below.
    public void sort(List<E> list) {
        sort(list, 0, list.size());
        assert Util.isSorted(list, comparator);
    }

    // Sort a range of the given list (last index is to-1).
    public abstract void sort(List<E> list, int from, int to);

    // Helper method: swap two positions in a list.
    public static <E> void swap(List<E> list, int i, int j) {
        E atI = list.get(i);
        E atJ = list.get(j);
        list.set(i, atJ);
        list.set(j, atI);
    }
}
