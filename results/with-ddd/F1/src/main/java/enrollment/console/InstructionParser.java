package enrollment.console;

import enrollment.domain.RuleViolation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parses one line of input into an Instruction (R33–R37, R40).
 * Returns empty if the line is blank (R37).
 * Throws RuleViolation with "invalid instruction" on parse errors.
 */
public class InstructionParser {

    /** Parse result: name + values. */
    public record Parsed(String name, List<String> values) {}

    private static final String INVALID = "invalid instruction";

    /**
     * Parse a line. Returns empty for blank lines.
     */
    public Optional<Parsed> parse(String line) {
        String trimmed = line.strip();
        if (trimmed.isEmpty()) return Optional.empty();

        // Must end with ;
        if (!trimmed.endsWith(";"))
            throw new RuleViolation(INVALID);

        String withoutSemi = trimmed.substring(0, trimmed.length() - 1).stripTrailing();

        // Find opening parenthesis — there may be spaces before it (R33)
        int parenOpen = withoutSemi.indexOf('(');
        if (parenOpen < 0) throw new RuleViolation(INVALID);

        String name = withoutSemi.substring(0, parenOpen).strip();
        if (name.isEmpty()) throw new RuleViolation(INVALID);

        // Must end with closing parenthesis
        if (!withoutSemi.endsWith(")")) throw new RuleViolation(INVALID);
        String argsStr = withoutSemi.substring(parenOpen + 1, withoutSemi.length() - 1);

        List<String> values = parseArgs(argsStr);
        return Optional.of(new Parsed(name, values));
    }

    /**
     * Split the argument string into individual values.
     * Values are comma-separated. Each value may be quoted (straight or typographic).
     * Spaces around values are ignored (R35).
     */
    private List<String> parseArgs(String argsStr) {
        List<String> result = new ArrayList<>();
        String s = argsStr.strip();
        if (s.isEmpty()) {
            return result; // no args
        }

        int i = 0;
        while (i <= s.length()) {
            // Skip leading spaces
            while (i < s.length() && s.charAt(i) == ' ') i++;
            if (i >= s.length()) break;

            char ch = s.charAt(i);
            String value;

            if (ch == '"' || ch == '\u201C') {
                // Quoted value — may use straight or opening typographic quote;
                // closing is " or \u201D
                char openQuote = ch;
                i++; // skip opening quote
                int start = i;
                while (i < s.length()) {
                    char c = s.charAt(i);
                    if (c == '"' || c == '\u201D') break;
                    i++;
                }
                if (i >= s.length()) throw new RuleViolation(INVALID); // unclosed quote
                value = s.substring(start, i);
                i++; // skip closing quote
            } else {
                // Unquoted value: up to the next comma or end of string
                int start = i;
                while (i < s.length() && s.charAt(i) != ',') i++;
                value = s.substring(start, i).strip();
                if (value.isEmpty()) {
                    // Empty unquoted token between commas — still record it
                }
            }

            result.add(value);

            // After the value, skip spaces then expect comma or end
            while (i < s.length() && s.charAt(i) == ' ') i++;
            if (i < s.length()) {
                if (s.charAt(i) == ',') {
                    i++; // consume comma
                } else {
                    throw new RuleViolation(INVALID); // unexpected character
                }
            }
        }
        return result;
    }
}
