package sorting;

import java.util.Comparator;
import java.util.List;

import util.Util;

// This interface abstracts over the pivot selection strategy in quicksort.
// We provide several implementations:
// - QuicksortPivotSelector.FIRST
// - QuicksortPivotSelector.RANDOM
// - QuicksortPivotSelector.MEDIAN_OF_THREE
// - QuicksortPivotSelector.ADAPTIVE
public interface QuicksortPivotSelector {
    public <E> int pivotIndex(List<E> list, int from, int to, Comparator<? super E> cmp);

    // A pivot selector that chooses the first element.
    static final QuicksortPivotSelector FIRST = new QuicksortPivotSelector() {
        public <E> int pivotIndex(List<E> list, int from, int to, Comparator<? super E> comparator) {
            return from;
        }
    };

    // A pivot selector that chooses a random index.
    static final QuicksortPivotSelector RANDOM = new QuicksortPivotSelector() {
        public <E> int pivotIndex(List<E> list, int from, int to, Comparator<? super E> comparator) {
            return Util.randRange(from, to);
        }
    };

    public static <E> int medianOfThreeIndex(List<E> array, Comparator<? super E> comparator, int i, int j, int k) {
        // Out of the below three Booleans, two must be equal. (Why?)
        boolean c = comparator.compare(array.get(i), array.get(j)) <= 0; //i mindre eller samma som j
        boolean d = comparator.compare(array.get(j), array.get(k)) <= 0;
        boolean e = comparator.compare(array.get(k), array.get(i)) <= 0;

        if (c == d)
            // The sequence a[i], a[j], a[k] ascending or descending.
            return j;

        if (d == e)
            // The sequence a[j], a[k], a[i] ascending or descending.
            return k;

        // The sequence a[k], a[i], a[j] is ascending or descending.
        return i;
    }

    // A pivot selector that uses median of three.
    static final QuicksortPivotSelector MEDIAN_OF_THREE = new QuicksortPivotSelector() {
        public <E> int pivotIndex(List<E> array, int from, int to, Comparator<? super E> comparator) {
            // We choose the median between the first, middle, last index.
            return medianOfThreeIndex(array, comparator, from, (from + to - 1) / 2, to - 1);
        }
    };

    // A pivot selector that adapts to the range size.
    static final QuicksortPivotSelector ADAPTIVE = new QuicksortPivotSelector() {
        public <E> int pivotIndex(List<E> array, int from, int to,
            Comparator<? super E> comparator) {
            // For small arrays, just pick first element.
            int size = to - from;
            if (size < 10)
                return FIRST.pivotIndex(array, from, to, comparator);

            int lo = from;
            int hi = to - 1;
            int mid = (lo + hi) / 2;

            // For medium arrays, pick median-of-three.
            if (size < 100)
                return medianOfThreeIndex(array, comparator, lo, mid, hi);

            // For large arrays, pick median-of-three of median-of-three.
            int d = size / 8;
            int i = medianOfThreeIndex(array, comparator, lo, lo + d, lo + 2 * d);
            int j = medianOfThreeIndex(array, comparator, hi, hi - d, hi - 2 * d);
            int k = medianOfThreeIndex(array, comparator, mid - d, mid, mid + d);
            return medianOfThreeIndex(array, comparator, i, j, k);
        }
    };

}
