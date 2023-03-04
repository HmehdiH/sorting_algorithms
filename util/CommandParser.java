package util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

// You don't have to look at this class!
public class CommandParser implements AutoCloseable {
    private Scanner stdin;
    private LinkedList<String> arguments;

    private ArrayList<String> parseErrors = new ArrayList<String>();
    private ArrayList<String> requiredFlags = new ArrayList<String>();
    private ArrayList<String> optionalFlags = new ArrayList<String>();

    public CommandParser(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Note: You can also pass command-line arguments. Use --help to learn more.");
            stdin = new Scanner(System.in);
        } else
            arguments = new LinkedList<>(Arrays.asList(args));
    }

    public String getString(String flag, String prompt) {
        return getArg(flag, prompt, null);
    }

    public String getString(String flag, String prompt, String defaultValue) {
        return getArg(flag, prompt, defaultValue);
    }

    private int getInt(String flag, String prompt, String defaultValue) {
        String s = getArg(flag, prompt, defaultValue);
        try {
            return Integer.parseInt(s.replace(",", "").replace("_", ""));
        } catch (NumberFormatException e) {
            return parsingError(-1, String.format("Value for %s %s is not an integer", flag, s));
        }
    }

    public int getInt(String flag, String prompt) {
        return getInt(flag, prompt, null);
    }

    public int getInt(String flag, String prompt, int defaultValue) {
        return getInt(flag, prompt, Integer.toString(defaultValue));
    }

    private float getFloat(String flag, String prompt, String defaultValue) {
        String s = getArg(flag, prompt, defaultValue);
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return parsingError(0.0f / 0, String.format("Value for %s %s is not a number", flag, s));
        }
    }

    public float getFloat(String flag, String prompt) {
        return getFloat(flag, prompt, null);
    }

    public float getFloat(String flag, String prompt, float defaultValue) {
        return getFloat(flag, prompt, Float.toString(defaultValue));
    }

    private boolean getBoolean(String flag, String prompt, String defaultValue) {
        String s = getArg(flag, prompt, defaultValue).toLowerCase();
        if (Arrays.asList("true", "t", "yes", "y", "1").contains(s))
            return true;
        if (Arrays.asList("false", "f", "no", "n", "0").contains(s))
            return false;
        return parsingError(false, String.format("Value for %s %s is not true/false", flag, s));
    }

    public boolean getBoolean(String flag, String prompt) {
        return getBoolean(flag, prompt, null);
    }

    public boolean getBoolean(String flag, String prompt, boolean defaultValue) {
        return getBoolean(flag, prompt, Boolean.toString(defaultValue));
    }

    private String getArg(String flag, String prompt, String defaultValue) {
        if (defaultValue != null)
            prompt = String.format("%s (default: %s)", prompt, defaultValue);

        (defaultValue == null ? requiredFlags : optionalFlags).add(String.format("%s: %s", flag, prompt));

        if (stdin != null) {
            String s;
            // If there's no default value, repeatedly ask until we
            // get a value
            do {
                System.out.format(flag + ": " + prompt + "? ");
                s = stdin.nextLine().trim();
            } while (s.isEmpty() && defaultValue == null);

            return s.isEmpty() ? defaultValue : s;
        } else {
            Iterator<String> iter = arguments.iterator();
            while (iter.hasNext()) {
                String arg = iter.next();
                if (isFlag(arg)) {
                    if (arg.equals(flag)) {
                        iter.remove();
                        try {
                            arg = iter.next();
                            if (isFlag(arg))
                                throw new NoSuchElementException();
                        } catch (NoSuchElementException e) {
                            return parsingError("", String.format("Missing value for %s: %s", flag, prompt));
                        }
                        iter.remove();
                        return arg;
                    } else {
                        // Skip over the next argument -- it's the value for the flag
                        try {
                            iter.next();
                        } catch (NoSuchElementException e) {
                            // Another flag is missing its value, but
                            // this will be taken care of when that
                            // flag is handled
                        }
                    }
                } else if (!isFlag(flag)) {
                    iter.remove();
                    return arg;
                }
            }
            if (defaultValue == null)
                return parsingError("", String.format("Missing option %s: %s", flag, prompt));
            else
                return defaultValue;
        }
    }

    private boolean isFlag(String flag) {
        return flag.startsWith("-");
    }

    public <E> E parsingError(E value, String error) {
        parsingError(error);
        return value;
    }

    public void parsingError(String error) {
        parseErrors.add(error);
    }

    @Override
    public void close() {
        // if (stdin != null)
        // stdin.close();
        if (arguments != null && !arguments.isEmpty())
            parseErrors.add("The following arguments were not understood: " + String.join(" ", arguments));
        if (arguments != null && (arguments.contains("--help") || arguments.contains("-h"))) {
            if (!requiredFlags.isEmpty()) {
                System.err.println("The following arguments are required:");
                for (String line : requiredFlags)
                    System.err.println("  " + line);
                System.err.println();
            }

            if (!optionalFlags.isEmpty()) {
                System.err.println("The following optional arguments may be given:");
                for (String line : optionalFlags)
                    System.err.println("  " + line);
                System.err.println();
            }

            System.exit(1);
        } else if (!parseErrors.isEmpty()) {
            System.err.println("Error when parsing command line arguments:");
            for (String err : parseErrors)
                System.err.println("  " + err);
            System.exit(1);
        }
    }
}
