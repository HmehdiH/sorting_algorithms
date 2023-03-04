import java.io.File;
import java.io.IOException;
import java.util.AbstractList;
import java.util.Comparator;
import java.util.List;

import sorting.*;
import util.BinarySearch;
import util.IOIntArray;
import util.Util;
import util.Util.CountingLexicographicComparator;

// A data structure for efficiently searching in a text.
// First, a search index (the sorted suffix array) needs to be built (`build`).
// This can be cached on disk (`writeToDisk`, `readFromDisk`).
// If a search index already exists on disk, calling `readFromDisk` will be much faster than `build`.
// Finally, search queries can be answered (`searchForKey`).
public class SuffixArray {
    public final String textFilename;
    public final Text text;

    // Whether the suffix array should be sorted and searched using case-sensitive or case-insensitive comparison.
    // If you change this value, you need to rebuild your suffix arrays stored on disk!
    public static final boolean IS_CASE_INSENSITIVE = false;

    // The suffix array.
    // This is an instance of List<Integer>.
    // Its elements are all the indices of the text.
    // They are sorted according to the *suffix* of each index: the string starting from that position in the text.
    // (It is much cheaper to only store the starting position of each suffix.
    // We can always look up the actual suffix using `text` and the stored position.)
    private IOIntArray sortedSuffixStarts;

    public SuffixArray(String textFilename) throws IOException {
        this.textFilename = textFilename;
        text = new Text(this.textFilename);
    }

    public void build() {
        Util.printTiming("Initializing suffix array", () -> {
            sortedSuffixStarts = new IOIntArray(text.size());

            // Write all the possible suffix starts into `sortedSuffixStarts` (not yet sorted).
            for (int i = 0; i<text.size(); i++) {
                sortedSuffixStarts.set(i, i);
            }
        });

        final LexicographicComparator<Integer> suffixComparator = text.suffixComparator(IS_CASE_INSENSITIVE);
        CountingLexicographicComparator<Integer> counting = new CountingLexicographicComparator<>(suffixComparator);

        double time = Util.printTiming("Sorting suffix array", () -> {
            MultiKeyQuicksort<Integer> sorter = new MultiKeyQuicksort<>(counting, QuicksortPivotSelector.MEDIAN_OF_THREE);
            //Quicksort<Integer> sorter = new Quicksort<>(counting, QuicksortPivotSelector.FIRST);
            //InsertionSort<Integer> sorter = new InsertionSort<>(counting);
            // Construct and call one of your sorting algorithms.
            sorter.sort(sortedSuffixStarts, 0, text.size());

        });
        System.out.println("  * Comparison count: " + counting.numComparisons());
        System.out.println("  * Average comparison time: " + Util.formatSeconds(time / counting.numComparisons()));

        Util.printTiming("Checking sortedness", () -> {
            if (!Util.isSorted(sortedSuffixStarts, suffixComparator)) {
                throw new RuntimeException("Suffix array is not sorted!");
            } else {
                System.out.println("Suffix array is sorted!");
            }
        });
    }

    // The filename for the index file just replaces the extension of the text file by ".ix".
    public String indexFilename() {
        return textFilename.replaceAll("\\.[a-zA-Z0-9]*$", ".ix");
    }

    public boolean existsOnDisk() {
        return new File(indexFilename()).exists();
    }

    public void readFromDisk() throws IOException {
        Util.printTiming("Reading suffix array from disk", () -> {
            sortedSuffixStarts = new IOIntArray(text.size());
            sortedSuffixStarts.readFromDisk(indexFilename());
        });
    }

    public void writeToDisk() throws IOException {
        Util.printTiming("Writing suffix array to disk", () -> {
            sortedSuffixStarts.writeToDisk(indexFilename());
        });
    }

    // A wrapper around sortedSuffixStarts that uses actual (truncated) suffix strings instead of start position.
    // This list is "virtual" (also called a "view"): the suffix strings do not actually exist.
    // Instead, they are constructed on-demand when the `get` method is called.
    public List<String> sortedSuffixes(int maxLength) {
        return new AbstractList<String>() {
            public int size() {
                return sortedSuffixStarts.size();
            }

            public String get(int i) {
                int start = sortedSuffixStarts.get(i);
                return text.substring(start, start + maxLength);
            }
        };
    }

    // This method searches the text for a given key and outputs found matches.
    // It uses the suffix array for the search, which makes it be blazingly fast.
    // * `searchKey`: The string key to search for. Can be anything between a character and a whole sentence.
    // * `maxNumMatches`: Report at most these many matches.
    // * `context`: How many characters to print to the left and right of each match.
    // * `trimLines`: Whether to restrict the context to the same line(s) as the match.

    public void searchForKey(String searchKey, int maxNumMatches, int context, boolean trimLines) {
        //First of all, we only consider as many characters of each suffix as in the search key. That's the puropose of this line constructs:
        final List<String> sortedSuffixes = sortedSuffixes(searchKey.length());
        //The comparator to use in our binary search:
        final Comparator<String> c = IS_CASE_INSENSITIVE ? String.CASE_INSENSITIVE_ORDER : Comparator.naturalOrder();

        Util.printTimingNL("Searching for \"" + searchKey + "\"", () -> {

            int startIndex = BinarySearch.findIntervalStart(sortedSuffixes, searchKey, c);
            int endIndex = BinarySearch.findIntervalEnd(sortedSuffixes, searchKey, c);
            int matches = endIndex - startIndex;

            int toPrint = Math.min(matches, maxNumMatches);
            int omittedLines = matches - maxNumMatches;

            if (matches <= 0) {
                System.out.println("[no matches found]");

            } else {

                for (int i = 0; i < toPrint; i++) {
                    text.printKeywordInContext(sortedSuffixStarts.get(startIndex + i), sortedSuffixStarts.get(startIndex + i) + searchKey.length(), context, trimLines);
                }

                if (omittedLines > 0) {
                    System.out.println("[" + omittedLines + " matches omitted]");
                }

            }
        });
    }

    // Experiment with the SuffixArray class here.
    public static void main(String[] args) throws IOException {
        SuffixArray suffixArray = new SuffixArray("texts/bnc-large.txt");
        suffixArray.build();
        //suffixArray.writeToDisk();
        //suffixArray.readFromDisk();
        suffixArray.searchForKey("elevators", 10, 40, false);
    }
}
