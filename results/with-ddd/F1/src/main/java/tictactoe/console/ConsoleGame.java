package tictactoe.console;

import tictactoe.domain.Color;
import tictactoe.domain.Coordinate;
import tictactoe.domain.State;
import tictactoe.domain.TicTacToe;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Console adapter. Reads Coordinates, prints the board and the result,
 * and writes the board log if TICTACTOE_BOARD_LOG is set.
 * Only this class reads input or writes output; tictactoe.domain never does.
 */
public final class ConsoleGame {

    private final TicTacToe game;
    private final Scanner input;
    private final PrintStream output;
    private final String boardLogPath; // null when not set

    public ConsoleGame(TicTacToe game, Scanner input, PrintStream output, String boardLogPath) {
        this.game = game;
        this.input = input;
        this.output = output;
        this.boardLogPath = boardLogPath;
    }

    /** Runs the game loop until the game ends, then prints the final board and result. */
    public void run() {
        writeBoardLog(); // initial empty board

        while (!game.state().isOver()) {
            String line = input.nextLine().trim();
            Coordinate coordinate = parseCoordinate(line); // I9: validate input
            if (coordinate == null) {
                output.println("Invalid input: expected two numbers between 1 and 3, e.g. 2 3");
                continue;
            }
            boolean accepted = game.put(coordinate); // I3: reject occupied square
            if (!accepted) {
                output.println("Invalid input: square " + coordinate.row() + " " + coordinate.column() + " is already occupied");
                continue;
            }
            writeBoardLog();
        }

        printFinalBoard();
        printResult();
    }

    /**
     * Parses a line of input as a Coordinate. Returns null if the input is invalid.
     * Invariant I9: input is invalid when it is not exactly two whole numbers,
     * or when a number is outside 1–3.
     */
    private Coordinate parseCoordinate(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length != 2) {
            return null;
        }
        int row, column;
        try {
            row = Integer.parseInt(parts[0]);
            column = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
        if (row < 1 || row > 3 || column < 1 || column > 3) {
            return null;
        }
        return new Coordinate(row, column);
    }

    /** Prints the 3-line final board: one character per square, no spaces. */
    private void printFinalBoard() {
        for (int row = 1; row <= 3; row++) {
            StringBuilder sb = new StringBuilder(3);
            for (int col = 1; col <= 3; col++) {
                Color color = game.colorAt(new Coordinate(row, col));
                sb.append(colorChar(color));
            }
            output.println(sb.toString());
        }
    }

    /** Prints the result line: "X wins", "O wins", or "Draw". */
    private void printResult() {
        State state = game.state();
        if (state == State.X_WINS) {
            output.println("X wins");
        } else if (state == State.O_WINS) {
            output.println("O wins");
        } else {
            output.println("Draw");
        }
    }

    private char colorChar(Color color) {
        return switch (color) {
            case X -> 'X';
            case O -> 'O';
            case EMPTY -> '.';
        };
    }

    /** Writes one line to the board log if TICTACTOE_BOARD_LOG is set. */
    private void writeBoardLog() {
        if (boardLogPath == null) return;
        String json = buildBoardLogJson();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(boardLogPath, true))) {
            writer.write(json);
            writer.newLine();
        } catch (IOException e) {
            // Board log writes must not crash the game; silently ignore.
        }
    }

    /**
     * Builds the JSON object for the current board state.
     * The state uses the board-log names from game-states.md (IN_PROGRESS, X_WINS, O_WINS, DRAW).
     */
    private String buildBoardLogJson() {
        List<String> pieces = new ArrayList<>();
        for (Coordinate c : Coordinate.all()) {
            Color color = game.colorAt(c);
            if (color != Color.EMPTY) {
                pieces.add("{\"row\":" + c.row() + ",\"column\":" + c.column() + ",\"color\":\"" + color.name() + "\"}");
            }
        }
        String boardArray = "[" + String.join(",", pieces) + "]";
        String logState = toLogState(game.state());
        return "{\"board\":" + boardArray + ",\"state\":\"" + logState + "\"}";
    }

    /** Translates domain State to board log state name (D11 / game-states.md). */
    private String toLogState(State state) {
        return switch (state) {
            case X_TO_PUT, O_TO_PUT -> "IN_PROGRESS";
            case X_WINS -> "X_WINS";
            case O_WINS -> "O_WINS";
            case DRAW -> "DRAW";
        };
    }
}
