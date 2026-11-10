package tictactoe.console;

import tictactoe.domain.Color;
import tictactoe.domain.Coordinate;
import tictactoe.domain.State;
import tictactoe.domain.ThreePiecesTicTacToe;

import java.util.Scanner;

/**
 * Console adapter for the three-pieces variant (D17).
 * Follows the console contract in REQUIREMENTS.md.
 *
 * In the put phase (X_TO_PUT, O_TO_PUT): reads one line with two numbers (row column).
 * In the move phase (X_TO_MOVE, O_TO_MOVE): reads one line with four numbers
 *   (fromRow fromColumn toRow toColumn).
 * Invalid input prints a line starting with "Invalid" and the same Player tries again.
 */
public final class ThreePiecesConsoleGame {

    private final ThreePiecesTicTacToe game;
    private final Scanner scanner;

    public ThreePiecesConsoleGame(ThreePiecesTicTacToe game, Scanner scanner) {
        this.game = game;
        this.scanner = scanner;
    }

    /** Runs the game until it is over, then prints the final board and result. */
    public void run() {
        while (!game.state().isOver()) {
            if (game.state().isMoving()) {
                handleMove();
            } else {
                handlePut();
            }
        }
        printFinalBoard();
        printResult();
    }

    private void handlePut() {
        String line = scanner.nextLine();
        Coordinate coordinate = parsePut(line);
        if (coordinate == null) {
            System.out.println("Invalid input: enter row and column each from 1 to 3, e.g. \"2 3\"");
            return;
        }
        boolean placed = game.put(coordinate);
        if (!placed) {
            System.out.println("Invalid move: square " + coordinate + " is already occupied");
        }
    }

    private void handleMove() {
        String line = scanner.nextLine();
        Coordinate[] coords = parseMove(line);
        if (coords == null) {
            System.out.println("Invalid input: enter fromRow fromColumn toRow toColumn each from 1 to 3, e.g. \"1 1 2 3\"");
            return;
        }
        boolean moved = game.move(coords[0], coords[1]);
        if (!moved) {
            System.out.println("Invalid move: check that the origin holds your piece and the target is empty");
        }
    }

    /**
     * Parses a put-phase line into a Coordinate.
     * Returns null when the input is invalid (I9): not exactly two whole numbers, or either outside 1–3.
     */
    private Coordinate parsePut(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length != 2) {
            return null;
        }
        try {
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            if (row < 1 || row > 3 || col < 1 || col > 3) return null;
            return new Coordinate(row, col);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses a move-phase line into [fromCoordinate, toCoordinate].
     * Returns null when the input is invalid: not exactly four whole numbers, or any outside 1–3.
     */
    private Coordinate[] parseMove(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length != 4) {
            return null;
        }
        try {
            int fromRow = Integer.parseInt(parts[0]);
            int fromCol = Integer.parseInt(parts[1]);
            int toRow   = Integer.parseInt(parts[2]);
            int toCol   = Integer.parseInt(parts[3]);
            if (fromRow < 1 || fromRow > 3 || fromCol < 1 || fromCol > 3) return null;
            if (toRow   < 1 || toRow   > 3 || toCol   < 1 || toCol   > 3) return null;
            return new Coordinate[]{new Coordinate(fromRow, fromCol), new Coordinate(toRow, toCol)};
        } catch (NumberFormatException e) {
            return null;
        }
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
            default     -> throw new IllegalStateException("Game is not over or unexpected state: " + state);
        };
        System.out.println(result);
    }
}
