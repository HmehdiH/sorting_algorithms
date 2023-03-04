package sorting;

import java.util.Comparator;

// A comparator for comparing lexicographically in a type E of sequences.
// The class is abstract, waiting for an implementation of comparing two elements at a given position.
//
// For example, if E is lists of integers and comparing them at a given position is implemented as expected,
// then [3, 1, 2, 1] and [3, 1, 1, 2] first differ in position 2, and 2 > 1, so the second list comes first.
public abstract class LexicographicComparator<E> implements Comparator<E> {
    // Compare x and y at position k.
    public abstract int compare(E x, E y, int k);

    // Compare x and y lexicographically.
    // This compares them at position 0, 1, 2, ... until we find a difference.
    public int compare(E x, E y) {
        // Avoid looping forever when x and y are the same.
        if (x.equals(y))
            return 0;

        // Loop over all positions until we find a difference.
        for (int k = 0;; k++) {
            int c = compare(x, y, k);
            if (c != 0)
                return c;
        }
    }
}
