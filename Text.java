import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import sorting.LexicographicComparator;
import util.Util;

public class Text {
    private char[] text;

    public Text(String filename) throws IOException {
        Util.printTiming("Reading file " + filename, () -> {
            text = Files.readString(Path.of(filename)).toCharArray();
        });
    }

    public int size() {
        return text.length;
    }

    public CharSequence suffix(int start) {
        return CharBuffer.wrap(text, start, text.length - start);
    }

    public String substring(int start, int end) {
        return new String(text, Math.max(0, start), Math.min(text.length, end) - start);
    }

    public LexicographicComparator<Integer> suffixComparator(boolean ignoreCase) {
        return new LexicographicComparator<Integer>() {
            // We inline a lot of potential abstraction in this method to make it faster.
            // For example, we could have parameterized this by an arbitrary character comparator.
            // However, the JVM is not smart enough to optimize this out.
            public int compare(Integer x, Integer y, int k) {
                int i = x + k;
                int j = y + k;

                if (i < text.length && j < text.length) {
                    char c = text[i];
                    char d = text[j];
                    if (ignoreCase) {
                        c = Character.toLowerCase(c);
                        d = Character.toLowerCase(d);
                    }
                    return Character.compare(c, d);
                }
                if (i < text.length)
                    return 1;
                if (j < text.length)
                    return -1;
                return 0;
            }
        };
    }

    // Print a match given by positions `start` and `end`.
    // We add `context` many bytes of context before and after.
    // The flag `trimLines` decides if we cut to only the lines of the match.
    public void printKeywordInContext(int start, int end, int context, boolean trimLines) {
        String prefix = substring(start - context, start);
        String found = substring(start, end);
        String suffix = substring(end, end + context);

        // Replace line breaks in the match.
        // Suppress carriage return from the output.
        found = found.replace('\n', ' ').replace('\r', ' ');
        prefix = prefix.replace("\r", "");
        suffix = suffix.replace("\r", "");

        // If config.trimLines is set, cut out any previous and following lines.
        if (trimLines) {
            int i = prefix.lastIndexOf('\n');
            if (i != -1)
                prefix = prefix.substring(i + 1);
            i = suffix.indexOf('\n');
            if (i != -1)
                suffix = suffix.substring(0, i);
        } else {
            prefix = prefix.replace('\n', ' ');
            suffix = suffix.replace('\n', ' ');
        }

        // Print out the match.
        System.out.printf(
            "%8d: %" + context + "s|%s|%-" + context + "s\n",
            start, prefix, found, suffix);
    }
}
