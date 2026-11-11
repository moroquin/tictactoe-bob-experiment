package tictactoe;

/**
 * Immutable board for the three-pieces variant.
 * Each player has exactly 3 pieces. Rounds 1–6 are puts; from round 7 onward
 * the current player moves one of their own pieces to an empty square.
 * There is no draw: the game continues until someone wins.
 */
final class ThreePiecesBoard {

    private static final int PIECES_PER_PLAYER = 3;

    private final Color[][] squares;
    private final State state;
    private final Color currentPlayer;
    /** Total number of valid puts that have occurred (0..6). */
    private final int piecesPlaced; // X pieces placed + O pieces placed

    /** Creates a fresh empty board; X goes first. */
    ThreePiecesBoard() {
        this.squares = new Color[3][3];
        this.state = State.IN_PROGRESS;
        this.currentPlayer = Color.X;
        this.piecesPlaced = 0;
    }

    private ThreePiecesBoard(Color[][] squares, State state, Color currentPlayer, int piecesPlaced) {
        this.squares = squares;
        this.state = state;
        this.currentPlayer = currentPlayer;
        this.piecesPlaced = piecesPlaced;
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
     * True while each player still has pieces left to place (rounds 1–6).
     */
    boolean isPutPhase() {
        return piecesPlaced < PIECES_PER_PLAYER * 2;
    }

    /**
     * Places the current player's piece at (row, col).
     * Only valid during the put phase.
     * Throws IllegalArgumentException on invalid input.
     */
    ThreePiecesBoard place(int row, int col) {
        if (state != State.IN_PROGRESS) {
            throw new IllegalArgumentException("Game is already over");
        }
        if (!isPutPhase()) {
            throw new IllegalArgumentException("Put phase is over; use move instead");
        }
        if (row < 1 || row > 3 || col < 1 || col > 3) {
            throw new IllegalArgumentException("Coordinates out of range");
        }
        if (squares[row - 1][col - 1] != null) {
            throw new IllegalArgumentException("Square already occupied");
        }

        Color[][] next = copySquares();
        next[row - 1][col - 1] = currentPlayer;

        State nextState = computeState(next, currentPlayer, false);
        Color nextPlayer = nextState == State.IN_PROGRESS ? currentPlayer.other() : currentPlayer;
        return new ThreePiecesBoard(next, nextState, nextPlayer, piecesPlaced + 1);
    }

    /**
     * Moves the current player's piece from (fromRow, fromCol) to (toRow, toCol).
     * Only valid during the move phase.
     * Throws IllegalArgumentException on invalid input.
     */
    ThreePiecesBoard move(int fromRow, int fromCol, int toRow, int toCol) {
        if (state != State.IN_PROGRESS) {
            throw new IllegalArgumentException("Game is already over");
        }
        if (isPutPhase()) {
            throw new IllegalArgumentException("Still in put phase; use place instead");
        }
        if (fromRow < 1 || fromRow > 3 || fromCol < 1 || fromCol > 3
                || toRow < 1 || toRow > 3 || toCol < 1 || toCol > 3) {
            throw new IllegalArgumentException("Coordinates out of range");
        }
        if (squares[fromRow - 1][fromCol - 1] != currentPlayer) {
            throw new IllegalArgumentException("Origin does not hold a piece of the current player");
        }
        if (squares[toRow - 1][toCol - 1] != null) {
            throw new IllegalArgumentException("Target square is not empty");
        }

        Color[][] next = copySquares();
        next[fromRow - 1][fromCol - 1] = null;
        next[toRow - 1][toCol - 1] = currentPlayer;

        State nextState = computeState(next, currentPlayer, true);
        Color nextPlayer = nextState == State.IN_PROGRESS ? currentPlayer.other() : currentPlayer;
        return new ThreePiecesBoard(next, nextState, nextPlayer, piecesPlaced);
    }

    private Color[][] copySquares() {
        Color[][] copy = new Color[3][3];
        for (int r = 0; r < 3; r++) {
            copy[r] = squares[r].clone();
        }
        return copy;
    }

    private static State computeState(Color[][] sq, Color last, boolean movePhase) {
        if (hasWon(sq, last)) {
            return last == Color.X ? State.X_WINS : State.O_WINS;
        }
        // No draw in this variant (movePhase or put phase — we only check win)
        return State.IN_PROGRESS;
    }

    private static boolean hasWon(Color[][] sq, Color c) {
        for (int r = 0; r < 3; r++) {
            if (sq[r][0] == c && sq[r][1] == c && sq[r][2] == c) return true;
        }
        for (int col = 0; col < 3; col++) {
            if (sq[0][col] == c && sq[1][col] == c && sq[2][col] == c) return true;
        }
        if (sq[0][0] == c && sq[1][1] == c && sq[2][2] == c) return true;
        if (sq[0][2] == c && sq[1][1] == c && sq[2][0] == c) return true;
        return false;
    }
}
