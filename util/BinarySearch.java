package util;

import java.util.Comparator;
import java.util.List;

// These functions should be familiar to you from Lab 1!
// There are only two differences:
// * They operate on lists instead of arrays.
// * They do not return -1 if the key is not in the list.
public class BinarySearch {

    // Find the first possible insertion point for the given search key.
    public static <A> int findIntervalStart(List<A> list, A key, Comparator<A> comparator) {
        int from = 0;
        int to = list.size();
        while (from < to) {
            int mid = (to + from) / 2;
            if (comparator.compare(key, list.get(mid)) <= 0)
                to = mid;
            else
                from = mid + 1;
        }
        return from;
    }

    // Find the last possible insertion point for the given search key.
    public static <A> int findIntervalEnd(List<A> list, A key, Comparator<A> comparator) {
        int from = 0;
        int to = list.size();
        while (from < to) {
            int mid = (to + from) / 2;
            if (comparator.compare(key, list.get(mid)) < 0)
                to = mid;
            else
                from = mid + 1;
        }
        return from;
    }

}
