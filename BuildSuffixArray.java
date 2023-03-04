import java.io.IOException;

import util.CommandParser;

public class BuildSuffixArray {

    public static void main(String[] args) throws IOException {
        String textFilename;
        try (CommandParser parser = new CommandParser(args)) {
            textFilename = parser.getString("-f", "Path to text file");
        }

        SuffixArray suffixArray = new SuffixArray(textFilename);
        suffixArray.build();
        suffixArray.writeToDisk();
    }

}
