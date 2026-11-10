package tictactoe;

import java.util.Scanner;

/**
 * Entry point. Drives the game loop and handles all console I/O.
 *
 * Console contract (from REQUIREMENTS.md):
 *   - One line per move: "row col" (1-based, space-separated).
 *   - Invalid input → print a line starting with "Invalid", re-prompt same player.
 *   - On game over: print the board (3 lines, one row each, chars X/O/.), then
 *     the result line ("X wins", "O wins", or "Draw"). Then exit 0.
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Game game = new Game();

        System.out.println("Tic-Tac-Toe: enter moves as 'row col' (1-3).");

        while (!game.isOver()) {
            System.out.printf("Player %c, enter your move: ", game.currentPlayer());
            String line = scanner.hasNextLine() ? scanner.nextLine() : null;

            if (line == null) {
                // End of input stream — treat as invalid and break to avoid infinite loop.
                System.out.println("Invalid input: no more input.");
                break;
            }

            String[] parts = line.trim().split("\\s+");
            if (parts.length != 2) {
                System.out.println("Invalid input: enter exactly two numbers.");
                continue;
            }

            int row, col;
            try {
                row = Integer.parseInt(parts[0]);
                col = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input: numbers only.");
                continue;
            }

            if (!game.tryMove(row, col)) {
                System.out.println("Invalid move: square unavailable or out of range.");
                continue;
            }
        }

        // Print final board and result.
        System.out.println(game.board().render());
        switch (game.status()) {
            case X_WINS -> System.out.println("X wins");
            case O_WINS -> System.out.println("O wins");
            default     -> System.out.println("Draw");
        }
    }
}
