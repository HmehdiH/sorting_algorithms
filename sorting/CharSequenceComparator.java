package sorting;

import java.util.Comparator;

// A lexicographic comparator for strings.
//
// For example, "house" and "horse" first differ in position 2.
// Since 'r' < 'u', "horse" comes before "house".
public class CharSequenceComparator extends LexicographicComparator<String> {
    private Comparator<Character> charComparator;

    public CharSequenceComparator(Comparator<Character> charComparator) {
        this.charComparator = charComparator;
    }

    public CharSequenceComparator() {
        this(Comparator.naturalOrder());
    }

    public static final Comparator<Character> CASE_INSENSITIVE_CHAR_COMPARATOR = (x, y) -> {
        char a = Character.toLowerCase(x);
        char b = Character.toLowerCase(y);
        return Character.compare(a, b);
    };

    public CharSequenceComparator(boolean ignoreCase) {
        this(ignoreCase ? CASE_INSENSITIVE_CHAR_COMPARATOR : Comparator.naturalOrder());
    }

    public int compare(String x, String y, int k) {
        if (k < x.length() && k < y.length())
            return charComparator.compare(x.charAt(k), y.charAt(k));

        if (k < x.length())
            return 1;

        if (k < y.length())
            return -1;

        return 0;
    }
}
