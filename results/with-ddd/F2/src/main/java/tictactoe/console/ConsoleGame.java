package tictactoe.console;

import tictactoe.domain.Color;
import tictactoe.domain.Coordinate;
import tictactoe.domain.State;
import tictactoe.domain.TicTacToe;

import java.util.Scanner;

/**
 * Console adapter for TicTacToe. Follows the console contract in REQUIREMENTS.md exactly.
 * Reads one line per Put when the current Player is a Human Player (chooseCoordinate returns null).
 * For a PC Player (chooseCoordinate returns non-null) it uses that Coordinate directly (D13).
 * Rejects invalid input with a line starting with "Invalid" (I9).
 * When the game ends, prints the final board then the result line.
 */
public final class ConsoleGame {

    private final TicTacToe game;
    private final Scanner scanner;

    public ConsoleGame(TicTacToe game, Scanner scanner) {
        this.game = game;
        this.scanner = scanner;
    }

    /** Runs the game until it is over, then prints the final board and result. */
    public void run() {
        while (!game.state().isOver()) {
            // Ask the current Player to choose a Coordinate; null means read from input (Human).
            Coordinate chosen = game.currentPlayer().chooseCoordinate(game);
            if (chosen != null) {
                // PC Player: put directly (it always picks a valid empty square)
                game.put(chosen);
            } else {
                // Human Player: read from standard input
                String line = scanner.nextLine();
                Coordinate coordinate = parse(line);
                if (coordinate == null) {
                    System.out.println("Invalid input: enter row and column each from 1 to 3, e.g. \"2 3\"");
                    continue;
                }
                boolean placed = game.put(coordinate);
                if (!placed) {
                    // Square is already occupied (I3) — the game validated it
                    System.out.println("Invalid move: square " + coordinate + " is already occupied");
                }
            }
        }
        printFinalBoard();
        printResult();
    }

    /**
     * Parses a line into a Coordinate. Returns null when the input is invalid (I9):
     * not exactly two whole numbers, or either number outside 1–3.
     */
    private Coordinate parse(String line) {
        String trimmed = line.trim();
        String[] parts = trimmed.split("\\s+");
        if (parts.length != 2) {
            return null;
        }
        int row;
        int column;
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

    /** Prints the 3×3 board as three lines of X, O, or . (no spaces). */
    private void printFinalBoard() {
        for (int r = 1; r <= 3; r++) {
            StringBuilder sb = new StringBuilder(3);
            for (int c = 1; c <= 3; c++) {
                Color color = game.colorAt(new Coordinate(r, c));
                sb.append(color == Color.EMPTY ? '.' : color.name().charAt(0));
            }
            System.out.println(sb);
        }
    }

    private void printResult() {
        State state = game.state();
        String result = switch (state) {
            case X_WINS -> "X wins";
            case O_WINS -> "O wins";
            case DRAW   -> "Draw";
            default     -> throw new IllegalStateException("Game is not over: " + state);
        };
        System.out.println(result);
    }
}
