package tictactoe;

/**
 * Immutable representation of the 3x3 board.
 * rows and columns are 1-based (1..3).
 */
final class Board {

    // squares[row-1][col-1]: null = empty, otherwise the Color
    private final Color[][] squares;
    private final State state;
    private final Color currentPlayer;

    /** Creates a fresh empty board; X goes first. */
    Board() {
        this.squares = new Color[3][3];
        this.state = State.IN_PROGRESS;
        this.currentPlayer = Color.X;
    }

    private Board(Color[][] squares, State state, Color currentPlayer) {
        this.squares = squares;
        this.state = state;
        this.currentPlayer = currentPlayer;
    }

    State state() {
        return state;
    }

    Color currentPlayer() {
        return currentPlayer;
    }

    /** Returns the color at (row, col) or null if empty. row/col are 1-based. */
    Color at(int row, int col) {
        return squares[row - 1][col - 1];
    }

    /**
     * Returns a new Board after placing the current player's piece at (row, col).
     * Throws IllegalArgumentException if the move is invalid.
     */
    Board place(int row, int col) {
        if (state != State.IN_PROGRESS) {
            throw new IllegalArgumentException("Game is already over");
        }
        if (row < 1 || row > 3 || col < 1 || col > 3) {
            throw new IllegalArgumentException("Coordinates out of range");
        }
        if (squares[row - 1][col - 1] != null) {
            throw new IllegalArgumentException("Square already occupied");
        }

        Color[][] next = copySquares();
        next[row - 1][col - 1] = currentPlayer;

        State nextState = computeState(next, currentPlayer);
        Color nextPlayer = nextState == State.IN_PROGRESS ? currentPlayer.other() : currentPlayer;
        return new Board(next, nextState, nextPlayer);
    }

    private Color[][] copySquares() {
        Color[][] copy = new Color[3][3];
        for (int r = 0; r < 3; r++) {
            copy[r] = squares[r].clone();
        }
        return copy;
    }

    private static State computeState(Color[][] sq, Color last) {
        if (hasWon(sq, last)) {
            return last == Color.X ? State.X_WINS : State.O_WINS;
        }
        // check draw: all squares filled
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (sq[r][c] == null) return State.IN_PROGRESS;
            }
        }
        return State.DRAW;
    }

    private static boolean hasWon(Color[][] sq, Color c) {
        // rows
        for (int r = 0; r < 3; r++) {
            if (sq[r][0] == c && sq[r][1] == c && sq[r][2] == c) return true;
        }
        // columns
        for (int col = 0; col < 3; col++) {
            if (sq[0][col] == c && sq[1][col] == c && sq[2][col] == c) return true;
        }
        // main diagonal
        if (sq[0][0] == c && sq[1][1] == c && sq[2][2] == c) return true;
        // anti diagonal
        if (sq[0][2] == c && sq[1][1] == c && sq[2][0] == c) return true;
        return false;
    }
}
