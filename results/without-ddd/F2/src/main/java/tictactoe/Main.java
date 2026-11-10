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
 *
 * Arguments:
 *   (none)    — two Human Players (original behaviour).
 *   --vs-pc   — Human plays X, PC plays O.
 */
public class Main {

    public static void main(String[] args) {
        boolean vsPC = args.length > 0 && "--vs-pc".equals(args[0]);

        Scanner scanner = new Scanner(System.in);
        Game game = new Game();
        PcPlayer pc = vsPC ? new PcPlayer() : null;

        System.out.println("Tic-Tac-Toe: enter moves as 'row col' (1-3).");

        while (!game.isOver()) {
            char current = game.currentPlayer();

            // PC's turn (only in --vs-pc mode when it's O's turn)
            if (vsPC && current == 'O') {
                int[] move = pc.chooseMove(game.board(), 'O');
                game.tryMove(move[0], move[1]);
                continue;
            }

            // Human's turn
            System.out.printf("Player %c, enter your move: ", current);
            String line = scanner.hasNextLine() ? scanner.nextLine() : null;

            if (line == null) {
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
