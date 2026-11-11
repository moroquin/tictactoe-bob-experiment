package tictactoe;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Writes board state to a JSON Lines log file when TICTACTOE_BOARD_LOG is set.
 * Does nothing when the env variable is absent.
 */
final class BoardLog implements Closeable {

    private final PrintWriter writer;

    /** Opens the log file. Pass null or empty path to create a no-op log. */
    private BoardLog(PrintWriter writer) {
        this.writer = writer;
    }

    static BoardLog open() {
        String path = System.getenv("TICTACTOE_BOARD_LOG");
        if (path == null || path.isEmpty()) {
            return new BoardLog(null);
        }
        try {
            PrintWriter pw = new PrintWriter(
                new BufferedWriter(
                    new OutputStreamWriter(
                        new FileOutputStream(path, false),
                        StandardCharsets.UTF_8)));
            return new BoardLog(pw);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void write(Board board) {
        if (writer == null) return;
        writer.println(toJson(board));
        writer.flush();
    }

    void write(ThreePiecesBoard board) {
        if (writer == null) return;
        writer.println(toJsonThreePieces(board));
        writer.flush();
    }

    @Override
    public void close() {
        if (writer != null) writer.close();
    }

    // Produces: {"board":[{"row":1,"column":1,"color":"X"}],"state":"IN_PROGRESS"}
    private static String toJson(Board board) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"board\":[");
        boolean first = true;
        for (int row = 1; row <= 3; row++) {
            for (int col = 1; col <= 3; col++) {
                Color c = board.at(row, col);
                if (c != null) {
                    if (!first) sb.append(',');
                    sb.append("{\"row\":").append(row)
                      .append(",\"column\":").append(col)
                      .append(",\"color\":\"").append(c.name()).append("\"}");
                    first = false;
                }
            }
        }
        sb.append("],\"state\":\"").append(board.state().name()).append("\"}");
        return sb.toString();
    }

    private static String toJsonThreePieces(ThreePiecesBoard board) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"board\":[");
        boolean first = true;
        for (int row = 1; row <= 3; row++) {
            for (int col = 1; col <= 3; col++) {
                Color c = board.at(row, col);
                if (c != null) {
                    if (!first) sb.append(',');
                    sb.append("{\"row\":").append(row)
                      .append(",\"column\":").append(col)
                      .append(",\"color\":\"").append(c.name()).append("\"}");
                    first = false;
                }
            }
        }
        sb.append("],\"state\":\"").append(board.state().name()).append("\"}");
        return sb.toString();
    }
}
