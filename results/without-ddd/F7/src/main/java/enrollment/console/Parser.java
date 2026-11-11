package enrollment.console;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses one line of input into a name + argument list.
 * Follows R33–R37.
 */
public class Parser {

    public record Instruction(String name, List<String> args) {}

    /** Returns null when the line is blank (R37). Throws ParseException on error. */
    public static Instruction parse(String line) {
        // strip leading/trailing whitespace
        String s = line.strip();
        if (s.isEmpty()) return null;

        // find the '(' — spaces are allowed before it (R33)
        int paren = s.indexOf('(');
        if (paren < 0) throw new ParseException("missing '('");

        String rawName = s.substring(0, paren).strip();
        if (rawName.isEmpty()) throw new ParseException("missing instruction name");

        // must end with ';'
        if (!s.endsWith(";")) throw new ParseException("missing ';'");
        String rest = s.substring(paren + 1, s.length() - 1); // between '(' and ';'

        // rest must end with ')'
        if (!rest.endsWith(")")) throw new ParseException("missing ')'");
        rest = rest.substring(0, rest.length() - 1); // strip trailing ')'

        List<String> args = parseArgs(rest);
        return new Instruction(rawName.toLowerCase(), args);
    }

    /**
     * Splits the argument string by commas, respecting quoted strings.
     * Quotes may be straight (") or typographic (" ").
     * R34, R35.
     */
    private static List<String> parseArgs(String rest) {
        List<String> args = new ArrayList<>();
        if (rest.isBlank()) return args;

        int i = 0;
        int len = rest.length();
        while (i <= len) {
            // skip leading spaces
            while (i < len && rest.charAt(i) == ' ') i++;
            if (i >= len) {
                // trailing comma → empty token (invalid, but we accept the args as-is and
                // let the instruction handler deal with argument count)
                break;
            }
            char ch = rest.charAt(i);
            String token;
            if (ch == '"' || ch == '\u201C') {
                // quoted string — find matching close quote
                char closeQuote = (ch == '\u201C') ? '\u201D' : '"';
                int start = i + 1;
                int end = rest.indexOf(closeQuote, start);
                if (end < 0) {
                    // try straight quote as close as well (mixed quotes, R35)
                    end = rest.indexOf('"', start);
                }
                if (end < 0) throw new ParseException("unclosed quote");
                token = rest.substring(start, end);
                i = end + 1;
                // skip spaces after closing quote
                while (i < len && rest.charAt(i) == ' ') i++;
                if (i < len && rest.charAt(i) != ',')
                    throw new ParseException("unexpected character after closing quote");
                if (i < len) i++; // skip comma
            } else {
                // unquoted token: read until comma
                int start = i;
                while (i < len && rest.charAt(i) != ',') i++;
                token = rest.substring(start, i).strip();
                // validate no special characters (partial — R35 says these chars require quotes)
                if (token.contains("\"") || token.contains(";") || token.contains("(") || token.contains(")"))
                    throw new ParseException("unquoted token contains special characters");
                if (i < len) i++; // skip comma
            }
            args.add(token);
        }
        return args;
    }

    public static class ParseException extends RuntimeException {
        public ParseException(String msg) { super(msg); }
    }
}
