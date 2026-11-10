package tictactoe;

/**
 * Game state machine. Holds a Board and tracks whose turn it is.
 * The caller drives the loop; Game only knows the rules.
 */
public class Game {

    public enum Status { ONGOING, X_WINS, O_WINS, DRAW }

    private final Board board = new Board();
    private char currentPlayer = 'X';
    private Status status = Status.ONGOING;

    /** Returns whose turn it is: 'X' or 'O'. */
    public char currentPlayer() {
        return currentPlayer;
    }

    public Status status() {
        return status;
    }

    public Board board() {
        return board;
    }

    /**
     * Validates the move and, if valid, places the piece and updates status.
     *
     * @return true if the move was accepted, false if it was invalid.
     */
    public boolean tryMove(int row, int col) {
        if (row < 1 || row > 3 || col < 1 || col > 3) return false;
        if (!board.isEmpty(row, col)) return false;

        board.place(row, col, currentPlayer);

        if (board.hasWon(currentPlayer)) {
            status = (currentPlayer == 'X') ? Status.X_WINS : Status.O_WINS;
        } else if (board.isFull()) {
            status = Status.DRAW;
        } else {
            currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
        }
        return true;
    }

    public boolean isOver() {
        return status != Status.ONGOING;
    }
}
