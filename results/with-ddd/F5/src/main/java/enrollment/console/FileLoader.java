package enrollment.console;

import enrollment.domain.RuleViolation;
import enrollment.domain.access.Action;
import enrollment.application.AccessService;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Handles the Load(path) instruction (I24, CR-2).
 * Reads a UTF-8 file one line at a time, checks each instruction name is in
 * the allowed set, dispatches it, and counts correct and erroneous lines.
 * Empty lines count in the line number but not as correct or errors.
 */
public class FileLoader {

    /** Instruction names allowed in a load file (create-only subset, CR-2). */
    static final Set<String> ALLOWED_NAMES = Set.of(
            "user", "building", "classroom", "course",
            "student", "professor", "section", "enroll"
    );

    private final AccessService access;
    private final InstructionParser parser;
    private final Dispatcher dispatcher;
    private final PrintStream out;

    public FileLoader(AccessService access, InstructionParser parser,
                      Dispatcher dispatcher, PrintStream out) {
        this.access = access;
        this.parser = parser;
        this.dispatcher = dispatcher;
        this.out = out;
    }

    /**
     * Execute the Load(path) instruction.
     * Checks session and LOAD_FILE permission, then processes the file.
     * Writes error lines and the summary line.
     */
    public void load(String pathStr) {
        access.requireSession();
        access.requirePermission(Action.LOAD_FILE);
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(pathStr), StandardCharsets.UTF_8);
        } catch (IOException e) {
            out.println("Error: " + e.getMessage());
            return;
        }
        int correct = 0;
        int errors = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineNumber = i + 1;
            // Empty lines count in line number but not as correct or error
            if (line.isBlank()) continue;
            String errorReason = processLine(line);
            if (errorReason != null) {
                out.println("Error in line " + lineNumber + ": " + errorReason);
                errors++;
            } else {
                correct++;
            }
        }
        out.println("OK: load finished, " + correct + " correct and " + errors + " with errors");
    }

    /**
     * Process one non-blank line from the file.
     * Returns null on success, or the error reason string on failure.
     */
    private String processLine(String line) {
        // Step 1: parse
        InstructionParser.Parsed parsed;
        try {
            var opt = parser.parse(line);
            if (opt.isEmpty()) return null; // blank (shouldn't happen, already checked)
            parsed = opt.get();
        } catch (RuleViolation e) {
            return e.reason();
        }
        // Step 2: check allowed names
        if (!ALLOWED_NAMES.contains(parsed.name().toLowerCase())) {
            return "invalid instruction";
        }
        // Step 3: dispatch
        try {
            String response = dispatcher.dispatch(parsed);
            // null means Exit (not allowed in file, but name check above prevents it)
            if (response != null && response.startsWith("Error:")) {
                return response.substring("Error: ".length());
            }
            return null; // success
        } catch (RuleViolation e) {
            return e.reason();
        }
    }
}
