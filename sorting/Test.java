package sorting;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import util.CommandParser;
import util.IOIntArray;
import util.Util;
import util.Util.CountingLexicographicComparator;

public class Test {
    @SuppressWarnings("serial")
    static class TestException extends Exception {
    }

    public static void main(String[] args) throws Exception {
        // Default arguments.
        int size = 10000;
        float randomness = 1.0f;
        String sortingAlgorithmString = "i"; // insertion sort
        String pivotSelectorString = "f"; // take first
        SortingAlgorithm<Integer, ? super LexicographicComparator<Integer>> sorter = null;
        boolean test = false;

        // We use non-negative integers to test sorting.
        // For the lexicographic ordering (only used for multi-key quicksort),
        // we compare 4 bits at a time, starting with the most significant.
        LexicographicComparator<Integer> comparator = new LexicographicComparator<Integer>() {
            int[] bits = new int[8];
            {
                for (int k = 0; k != 8; ++k)
                    bits[k] = 0b1111 << (4 * (7 - k));
            }

            public int compare(Integer x, Integer y, int k) {
                int i = x & bits[k];
                int j = y & bits[k];
                return Integer.compare(i, j);
            }

            public int compare(Integer x, Integer y) {
                return Integer.compare(x, y);
            }
        };
        CountingLexicographicComparator<Integer> countingComparator = new CountingLexicographicComparator<Integer>(comparator);

        try (CommandParser parser = new CommandParser(args)) {
            test = parser.getBoolean("-t", "Test the sorting algorithm on lots of small arrays", false);
            if (!test) {
                size = parser.getInt("-n", "Size of test array", size);
                randomness = parser.getFloat("-r", "Randomness (between 0.0 and 1.0)", randomness);
            }
            sortingAlgorithmString = parser.getString("-a",
                "Sorting algorithm [(i)insertion sort / (q)uicksort / (m)ulti-key quicksort]", sortingAlgorithmString)
                .substring(0, 1).toLowerCase();
            if (sortingAlgorithmString.equals("i"))
                sorter = new InsertionSort<Integer>(countingComparator);
            else if (Set.of("q", "m").contains(sortingAlgorithmString)) {
                pivotSelectorString = parser.getString("-p",
                    "Pivot selector [take (f)irst / (r)andom / (m)edian-of-three / (a)daptive]", pivotSelectorString)
                    .substring(0, 1).toLowerCase();

                Map<String, QuicksortPivotSelector> pivotSelectors = Map.of(
                    "f", QuicksortPivotSelector.FIRST,
                    "m", QuicksortPivotSelector.MEDIAN_OF_THREE,
                    "r", QuicksortPivotSelector.RANDOM,
                    "a", QuicksortPivotSelector.ADAPTIVE);
                QuicksortPivotSelector pivotSelector = pivotSelectors.get(pivotSelectorString);
                if (pivotSelector == null)
                    parser.parsingError("Value for -p must be one of " + pivotSelectors.keySet());

                if (sortingAlgorithmString.equals("q"))
                    sorter = new Quicksort<Integer>(countingComparator, pivotSelector);
                else
                    sorter = new MultiKeyQuicksort<Integer>(countingComparator, pivotSelector);
            } else
                parser.parsingError("Value for -a must be i or q or m");
        }

        try {
            if (test)
                quickcheckSorter(sorter);
            else {
                double time = testSorter(sorter, size, randomness, false);
                System.out.println();
                System.out.format("Comparisons:      %d cmps\n", countingComparator.numComparisons());
                System.out.format("Comparison speed: %.0f cmps/ms\n", countingComparator.numComparisons() / (1000 * time));
            }
        } catch (TestException e) {
            System.exit(1);
        }
    }

    private static void quickcheckSorter(SortingAlgorithm<Integer, ? super CountingLexicographicComparator<Integer>> sorter) throws Exception {
        for (final int size : IntStream.rangeClosed(1, 30).toArray()) {
            for (final float randomness : new float[] { 0.0f, 0.25f, 1.0f }) {
                // Run each test 10 times to increase the chance of failure.
                for (int i = 0; i != 10; ++i) {
                    // Only display output if the test fails.
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try {
                        Util.withOutput(out, () -> testSorter(sorter, size, randomness, true));
                    } catch (TestException e) {
                        System.out.print(out);
                        System.exit(1);
                    } catch (Exception e) {
                        System.out.print(out);
                        throw e;
                    }
                }
            }
        }
        System.out.println("All tests passed!");
    }

    private static double testSorter(SortingAlgorithm<Integer, ? super CountingLexicographicComparator<Integer>> sorter, int size, float randomness,
        boolean printList) throws Exception {
        @SuppressWarnings("unchecked")
        List<Integer>[] list = (List<Integer>[]) new List<?>[1];

        BiFunction<String, Runnable, Double> printTiming = (description, f) -> {
            System.out.format("%-16s", description + ": ");
            System.out.flush();
            double time = Util.time(() -> f.run());
            System.out.format("%-9s", Util.formatSeconds(time));
            if (printList)
                System.out.print("  -->  " + list[0]);
            System.out.println();
            return time;
        };

        printTiming.apply("Building list", () -> {
            list[0] = new IOIntArray(size);
            for (int i = 0; i != list[0].size(); ++i)
                list[0].set(i, i);
        });
        printTiming.apply("Shuffling list", () -> Util.shuffleArray(list[0], randomness));
        double time = printTiming.apply("Sorting list", () -> sorter.sort(list[0]));

        @SuppressWarnings("unchecked")
        CountingLexicographicComparator<Integer> comparator = (CountingLexicographicComparator<Integer>) sorter.getComparator();
        if (!Util.isSorted(list[0], comparator.underlyingComparator())) {
            System.out.format("ERROR: list is NOT sorted!\n\n");
            throw new TestException();
        }

        for (int i = 0; i < list[0].size(); i++)
            if (list[0].get(i) != i) {
                System.err.format("ERROR: wrong value (at position %d): should be %d, but was %d\n", i, i, list[0].get(i));
                System.err.format("ERROR: list has wrong contents!\n");
                throw new TestException();
            }
        return time;
    }
}
