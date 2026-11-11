package tictactoe.domain;

/**
 * The aggregate root: the game itself. One Board, one Round, two Players.
 * Invariant I10: only TicTacToe changes a game; all mutators on Board, Square
 * and Round are package-private and only called from here.
 */
public final class TicTacToe {

    private final Board board;
    private final Round round;
    private State state;

    /**
     * Creates a new TicTacToe game. X always starts.
     *
     * @param playerX the Player who plays X
     * @param playerO the Player who plays O
     */
    public TicTacToe(Player playerX, Player playerO) {
        if (playerX.color() != Color.X) {
            throw new IllegalArgumentException("playerX must have Color X");
        }
        if (playerO.color() != Color.O) {
            throw new IllegalArgumentException("playerO must have Color O");
        }
        this.board = new Board();
        this.round = new Round(playerX, playerO);
        this.state = State.X_TO_PUT;
    }

    /**
     * Puts a Piece of the current Player's Color on the Square at the given Coordinate.
     * Returns true when the put succeeds; returns false and changes nothing when
     * the Square is already occupied (I3) or the game is already over (I8).
     */
    public boolean put(Coordinate coordinate) {
        if (state.isOver()) {
            return false; // I8: no put after the game ends
        }
        if (!board.isEmpty(coordinate)) {
            return false; // I3: reject put on occupied square
        }
        Color color = round.current().color();
        board.put(coordinate, new Piece(color)); // I5: the current Player's Color
        if (board.hasWin(color)) {              // I6: check win first
            state = (color == Color.X) ? State.X_WINS : State.O_WINS;
        } else if (board.isFull()) {            // I7: draw if full and no win
            state = State.DRAW;
        } else {
            round.next();                       // I4: alternate Players
            state = (round.current().color() == Color.X) ? State.X_TO_PUT : State.O_TO_PUT;
        }
        return true;
    }

    /** Returns the current State of the game. */
    public State state() {
        return state;
    }

    /** Returns the Player who puts next (only meaningful when the game is not over). */
    public Player currentPlayer() {
        return round.current();
    }

    /** Returns the Color at the given Coordinate (EMPTY if the Square is empty). */
    public Color colorAt(Coordinate coordinate) {
        return board.colorAt(coordinate);
    }
}
