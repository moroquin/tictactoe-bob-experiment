package enrollment.console;

import enrollment.domain.RuleViolation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * The console loop: reads instructions from stdin, dispatches them, writes responses to stdout.
 * Prompts (if any) go to stderr (R42).
 * Exits on Exit() or end of input (R43).
 */
public class ConsoleLoop {

    /** Sentinel: means the program should exit (Exit() instruction). */
    private static final String EXIT_SENTINEL = null;

    private final InstructionParser parser;
    private final Dispatcher dispatcher;
    private final PrintStream out;

    public ConsoleLoop(InstructionParser parser, Dispatcher dispatcher, PrintStream out) {
        this.parser = parser;
        this.dispatcher = dispatcher;
        this.out = out;
    }

    public void run(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            boolean shouldExit = handleLine(line);
            if (shouldExit) return;
        }
    }

    /**
     * Process one line. Returns true if the program should exit.
     */
    boolean handleLine(String line) {
        try {
            var parsedOpt = parser.parse(line);
            if (parsedOpt.isEmpty()) {
                // Blank line — ignore, produce no output (R37)
                return false;
            }
            InstructionParser.Parsed parsed = parsedOpt.get();
            // Check for Exit before dispatching
            if (parsed.name().equalsIgnoreCase("exit")) {
                // Exit() must have 0 args to be valid
                if (!parsed.values().isEmpty()) {
                    out.println("Error: invalid instruction");
                    return false;
                }
                return true; // exit
            }
            String response = dispatcher.dispatch(parsed);
            if (response == null) {
                return true; // exit sentinel from dispatcher
            }
            out.println(response);
            return false;
        } catch (RuleViolation e) {
            out.println("Error: " + e.reason());
            return false;
        }
    }
}
