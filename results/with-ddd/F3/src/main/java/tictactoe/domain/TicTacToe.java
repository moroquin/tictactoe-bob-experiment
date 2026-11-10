package tictactoe.domain;

/**
 * The game itself: one Board, one Round and two Players.
 * It is the aggregate root: the only object that changes a game (I10).
 */
public final class TicTacToe {

    private final Board board;
    private final Round round;
    private State state;

    public TicTacToe(Player playerX, Player playerO) {
        this.board = new Board();
        this.round = new Round(playerX, playerO);
        this.state = State.X_TO_PUT;
    }

    /**
     * Puts a Piece for the current Player on the Square at the given Coordinate.
     *
     * Returns false and changes nothing when:
     * - the game is already over (I8), or
     * - the Square is already occupied (I3).
     *
     * Otherwise returns true and updates the Board, Round and State.
     */
    public boolean put(Coordinate coordinate) {
        if (state.isOver()) {
            return false;  // I8
        }
        if (!board.isEmpty(coordinate)) {
            return false;  // I3
        }

        // I5: put a Piece of the current Player's Color
        Color color = round.current().color();
        board.put(coordinate, new Piece(color));

        // I6: check for Win first, then I7: check for Draw
        if (board.hasWin(color)) {
            state = (color == Color.X) ? State.X_WINS : State.O_WINS;
        } else if (board.isFull()) {
            state = State.DRAW;  // I7
        } else {
            round.next();  // I4
            state = (round.current().color() == Color.X) ? State.X_TO_PUT : State.O_TO_PUT;
        }

        return true;
    }

    public State state() {
        return state;
    }

    public Player currentPlayer() {
        return round.current();
    }

    public Color colorAt(Coordinate coordinate) {
        return board.colorAt(coordinate);
    }
}
