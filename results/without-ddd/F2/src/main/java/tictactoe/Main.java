package tictactoe;

import java.util.Scanner;

public final class Main {

    public static void main(String[] args) {
        boolean vsPC = args.length == 1 && "--vs-pc".equals(args[0]);
        Scanner scanner = new Scanner(System.in);

        try (BoardLog log = BoardLog.open()) {
            Board board = new Board();
            log.write(board);

            while (board.state() == State.IN_PROGRESS) {
                if (vsPC && board.currentPlayer() == Color.O) {
                    // PC's turn — play automatically
                    board = PcPlayer.play(board);
                    log.write(board);
                } else {
                    // Human's turn — read from console
                    System.out.println("Player " + board.currentPlayer() + ", enter row and column (e.g. 2 3):");
                    String line = scanner.hasNextLine() ? scanner.nextLine() : null;
                    if (line == null) {
                        System.out.println("Invalid input");
                        continue;
                    }

                    String[] parts = line.trim().split("\\s+");
                    if (parts.length != 2) {
                        System.out.println("Invalid input");
                        continue;
                    }

                    int row, col;
                    try {
                        row = Integer.parseInt(parts[0]);
                        col = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input");
                        continue;
                    }

                    if (row < 1 || row > 3 || col < 1 || col > 3) {
                        System.out.println("Invalid input");
                        continue;
                    }

                    if (board.at(row, col) != null) {
                        System.out.println("Invalid input");
                        continue;
                    }

                    board = board.place(row, col);
                    log.write(board);
                }
            }

            // Print final board
            for (int r = 1; r <= 3; r++) {
                StringBuilder row = new StringBuilder();
                for (int c = 1; c <= 3; c++) {
                    Color color = board.at(r, c);
                    row.append(color == null ? '.' : color.name().charAt(0));
                }
                System.out.println(row);
            }

            // Print result
            switch (board.state()) {
                case X_WINS -> System.out.println("X wins");
                case O_WINS -> System.out.println("O wins");
                case DRAW   -> System.out.println("Draw");
                default     -> throw new IllegalStateException("Unexpected state: " + board.state());
            }
        }
    }
}
