import java.io.IOException;
import java.util.Scanner;

import util.CommandParser;

// This class is designed it be run.
// It is a search prompt for searching in the specified text file.
// We assume that the suffix array has been built and stored on disk before.
// (For this, see `BuildSuffixArray`.)
public class RunSearchPrompt {
    public static final int context = 40;
    public static final boolean trimLines = false;

    public static void main(String[] args) throws IOException {
        // Parse program arguments.
        String textFilename;
        int numMatches;
        try (CommandParser parser = new CommandParser(args)) {
            textFilename = parser.getString("-f", "Path to text file");
            numMatches = parser.getInt("-n", "Number of matches to show", 10);
        }

        // Load suffix array from disk.
        SuffixArray suffixArray = new SuffixArray(textFilename);
        if (!suffixArray.existsOnDisk()) {
            System.out.println("No suffix array found on disk. Build one first using BuildSuffixArray.");
            System.exit(-1);
        }
        suffixArray.readFromDisk();

        // The main REPL (read-eval-print loop)
        @SuppressWarnings("resource")
        Scanner input = new Scanner(System.in);
        while (true) {
            // Read search key from input line, exit if there is no more input.
            System.out.print("Enter search key (CTRL-C/D/Z to quit): ");
            System.out.flush();
            if (!input.hasNextLine())
                break;
            String searchKey = input.nextLine();

            suffixArray.searchForKey(searchKey, numMatches, context, trimLines);
        }
    }
}
