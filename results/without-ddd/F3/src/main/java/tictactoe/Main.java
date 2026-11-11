package tictactoe;

import java.util.Scanner;

public final class Main {

    public static void main(String[] args) {
        boolean vsPC = args.length == 1 && "--vs-pc".equals(args[0]);
        boolean threePieces = args.length == 1 && "--three-pieces".equals(args[0]);
        Scanner scanner = new Scanner(System.in);

        if (threePieces) {
            runThreePiecesGame(scanner);
        } else {
            runStandardGame(scanner, vsPC);
        }
    }

    // -------------------------------------------------------------------------
    // Standard game (no arguments or --vs-pc)
    // -------------------------------------------------------------------------

    private static void runStandardGame(Scanner scanner, boolean vsPC) {
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

            printFinalBoard(board);
            printResult(board.state());
        }
    }

    // -------------------------------------------------------------------------
    // Three-pieces variant (--three-pieces)
    // -------------------------------------------------------------------------

    private static void runThreePiecesGame(Scanner scanner) {
        try (BoardLog log = BoardLog.open()) {
            ThreePiecesBoard board = new ThreePiecesBoard();
            log.write(board);

            while (board.state() == State.IN_PROGRESS) {
                if (board.isPutPhase()) {
                    // Put phase: read row and column
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

                } else {
                    // Move phase: read fromRow fromCol toRow toCol
                    System.out.println("Player " + board.currentPlayer()
                            + ", enter from-row from-col to-row to-col (e.g. 1 1 2 3):");
                    String line = scanner.hasNextLine() ? scanner.nextLine() : null;
                    if (line == null) {
                        System.out.println("Invalid input");
                        continue;
                    }

                    String[] parts = line.trim().split("\\s+");
                    if (parts.length != 4) {
                        System.out.println("Invalid input");
                        continue;
                    }

                    int fromRow, fromCol, toRow, toCol;
                    try {
                        fromRow = Integer.parseInt(parts[0]);
                        fromCol = Integer.parseInt(parts[1]);
                        toRow   = Integer.parseInt(parts[2]);
                        toCol   = Integer.parseInt(parts[3]);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input");
                        continue;
                    }

                    if (fromRow < 1 || fromRow > 3 || fromCol < 1 || fromCol > 3
                            || toRow < 1 || toRow > 3 || toCol < 1 || toCol > 3) {
                        System.out.println("Invalid input");
                        continue;
                    }

                    if (board.at(fromRow, fromCol) != board.currentPlayer()) {
                        System.out.println("Invalid input");
                        continue;
                    }

                    if (board.at(toRow, toCol) != null) {
                        System.out.println("Invalid input");
                        continue;
                    }

                    board = board.move(fromRow, fromCol, toRow, toCol);
                    log.write(board);
                }
            }

            printFinalThreeBoard(board);
            printResult(board.state());
        }
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    private static void printFinalBoard(Board board) {
        for (int r = 1; r <= 3; r++) {
            StringBuilder row = new StringBuilder();
            for (int c = 1; c <= 3; c++) {
                Color color = board.at(r, c);
                row.append(color == null ? '.' : color.name().charAt(0));
            }
            System.out.println(row);
        }
    }

    private static void printFinalThreeBoard(ThreePiecesBoard board) {
        for (int r = 1; r <= 3; r++) {
            StringBuilder row = new StringBuilder();
            for (int c = 1; c <= 3; c++) {
                Color color = board.at(r, c);
                row.append(color == null ? '.' : color.name().charAt(0));
            }
            System.out.println(row);
        }
    }

    private static void printResult(State state) {
        switch (state) {
            case X_WINS -> System.out.println("X wins");
            case O_WINS -> System.out.println("O wins");
            case DRAW   -> System.out.println("Draw");
            default     -> throw new IllegalStateException("Unexpected state: " + state);
        }
    }
}
