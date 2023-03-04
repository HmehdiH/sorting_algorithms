package util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import sorting.LexicographicComparator;
import sorting.SortingAlgorithm;

// You don't have to look at this class!
public class Util {

    ///////////////////////////////////////////////////////////////////////////
    // Random choices.

    // We use a fixed seed to make testing deterministic.
    private static final Random random = new Random(0);

    public static int randRange(int lo, int hi) {
        return (int) (lo + random.nextDouble() * (hi - lo));
    }

    // Fisher-Yates shuffle algorithm, but decides randomly when to swap elements
    public static void shuffleArray(List<Integer> list, float randomness) {
        int size = list.size();
        for (int index = 0; index < size; index++) {
            if (random.nextDouble() < randomness) {
                int other = randRange(index, size);
                SortingAlgorithm.swap(list, index, other);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Comparing and sorting

    public static <E> boolean isSorted(List<E> array, Comparator<? super E> cmp) {
        E prev = array.get(0);
        for (int i = 1; i < array.size(); i++) {
            E next = array.get(i);
            if (!(cmp.compare(prev, next) <= 0)) {
                System.err.format("ERROR: not sorted (at position %d): \"%s\" > \"%s\"\n", i, prev,
                    next);
                return false;
            }
            prev = next;
        }
        return true;
    }

    public static class CountingComparator<E> implements Comparator<E> {
        Comparator<E> comparator;
        long counter = 0;

        public CountingComparator(Comparator<E> comparator) {
            this.comparator = comparator;
        }

        public int compare(E x, E y) {
            counter++;
            return comparator.compare(x, y);
        }

        public Comparator<E> underlyingComparator() {
            return comparator;
        }

        public long numComparisons() {
            return this.counter;
        }
    }

    public static class CountingLexicographicComparator<E> extends LexicographicComparator<E> {
        LexicographicComparator<E> comparator;
        long counter = 0;

        public CountingLexicographicComparator(LexicographicComparator<E> comparator) {
            this.comparator = comparator;
        }

        public int compare(E x, E y) {
            counter++;
            return comparator.compare(x, y);
        }

        public int compare(E x, E y, int k) {
            counter++;
            return comparator.compare(x, y, k);
        }

        public LexicographicComparator<E> underlyingComparator() {
            return comparator;
        }

        public long numComparisons() {
            return this.counter;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Timing

    public static String formatWithPrecision(double value, int precision) {
        int d = (int) Math.floor(Math.log10(value));
        return String.format("%." + Math.max(0, 2 - d) + "f", value);
    }

    public static String formatSeconds(double time) {
        String[] prefixes = { "", "m", "μ", "n", "p" };
        for (int i = 0;; ++i) {
            if (time >= 1 || i == prefixes.length - 1)
                return formatWithPrecision(time, 2) + " " + prefixes[i] + "s";
            time *= 1e3;
        }
    }

    public static interface RunnableThrows<E extends Throwable> {
        void run() throws E;
    }

    public static interface BiFunctionThrows<A, B, R, E extends Throwable> {
        R apply(A a, B b) throws E;
    }

    public static <E extends Throwable> double time(RunnableThrows<E> task) throws E {
        long startTime = System.nanoTime();
        task.run();
        long endTime = System.nanoTime();
        return (double) (endTime - startTime) / 1.e9;
    }

    public static <E extends Throwable> double printTiming(String description, RunnableThrows<E> callback) throws E {
        System.out.print(description + ": ");
        System.out.flush();
        double time = Util.time(() -> callback.run());
        System.out.println(Util.formatSeconds(time));
        return time;
    }

    public static <E extends Throwable> double printTimingNL(String description, RunnableThrows<E> callback) throws E {
        System.out.println(description + ":");
        double time = Util.time(() -> callback.run());
        System.out.println(Util.formatSeconds(time));
        return time;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Output redirection

    public static <E extends Throwable> void withOutput(OutputStream out, RunnableThrows<E> task)
        throws E {
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        try {
            System.setOut(new PrintStream(out));
            System.setErr(new PrintStream(out));
            task.run();
        } finally {
            System.setOut(oldOut);
            System.setErr(oldErr);
        }
    }
}
