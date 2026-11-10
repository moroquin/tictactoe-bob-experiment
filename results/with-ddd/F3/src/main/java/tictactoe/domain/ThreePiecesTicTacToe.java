package tictactoe.domain;

/**
 * The three-pieces variant of TicTacToe. Each Player has exactly three Pieces.
 * Rounds 1–6 are Puts (as in the standard game). From round 7 on, the current Player
 * moves one of their own Pieces to an empty Square.
 *
 * There is no Draw: the game continues until someone wins (I15).
 *
 * This is the aggregate root for the three-pieces variant (D14). It uses the same
 * package-private mutators (Board.put, Board.move, Round.next) as TicTacToe (I10).
 */
public final class ThreePiecesTicTacToe {

    private static final int PIECES_PER_PLAYER = 3;

    private final Board board;
    private final Round round;
    private State state;

    /** Remaining Pieces each Player has not yet put on the Board (I11). */
    private int piecesRemainingX;
    private int piecesRemainingO;

    public ThreePiecesTicTacToe(Player playerX, Player playerO) {
        this.board = new Board();
        this.round = new Round(playerX, playerO);
        this.state = State.X_TO_PUT;
        this.piecesRemainingX = PIECES_PER_PLAYER;
        this.piecesRemainingO = PIECES_PER_PLAYER;
    }

    /**
     * Puts a Piece for the current Player on the Square at the given Coordinate.
     * Only accepted when the game is in state X_TO_PUT or O_TO_PUT (I12).
     *
     * Returns false and changes nothing when:
     * - the game state is not a put state (I12), or
     * - the Square is already occupied (I3).
     *
     * Otherwise returns true and updates the Board, Round and State.
     */
    public boolean put(Coordinate coordinate) {
        if (!isPutState()) {
            return false;  // I12: only accept puts in put states
        }
        if (!board.isEmpty(coordinate)) {
            return false;  // I3
        }

        Color color = round.current().color();
        board.put(coordinate, new Piece(color));

        // Decrement pieces remaining for current player (I11)
        decrementPiecesRemaining(color);

        // I14: check for Win
        if (board.hasWin(color)) {
            state = (color == Color.X) ? State.X_WINS : State.O_WINS;
            return true;
        }

        // No win: advance the round and determine next state
        round.next();
        Color nextColor = round.current().color();
        state = nextStateAfterPut(nextColor);
        return true;
    }

    /**
     * Moves a Piece of the current Player's Color from {@code from} to an empty {@code to}.
     * Only accepted when the game is in state X_TO_MOVE or O_TO_MOVE (I13).
     *
     * Returns false and changes nothing when:
     * - the game is not in a move state (I13),
     * - the origin does not hold a Piece of the current Player's Color (I13), or
     * - the target is not empty (I13).
     *
     * Otherwise returns true and updates the Board, Round and State.
     */
    public boolean move(Coordinate from, Coordinate to) {
        if (!state.isMoving()) {
            return false;  // I13: only accept moves in move states (also covers I8, since isOver() states are not isMoving())
        }

        Color color = round.current().color();

        // I13: origin must hold a Piece of the current Player's Color
        if (board.colorAt(from) != color) {
            return false;
        }
        // I13: target must be empty
        if (!board.isEmpty(to)) {
            return false;
        }

        board.move(from, to);

        // I14: check for Win after move
        if (board.hasWin(color)) {
            state = (color == Color.X) ? State.X_WINS : State.O_WINS;
            return true;
        }

        // No win: alternate
        round.next();
        Color nextColor = round.current().color();
        state = (nextColor == Color.X) ? State.X_TO_MOVE : State.O_TO_MOVE;
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

    // ---- private helpers ----

    private boolean isPutState() {
        return state == State.X_TO_PUT || state == State.O_TO_PUT;
    }

    private void decrementPiecesRemaining(Color color) {
        if (color == Color.X) {
            piecesRemainingX--;
        } else {
            piecesRemainingO--;
        }
    }

    /**
     * Determines the next State after a successful Put, given the next Player's Color.
     * If both players have 0 pieces remaining, transitions to a move state.
     */
    private State nextStateAfterPut(Color nextColor) {
        boolean allPlaced = (piecesRemainingX == 0 && piecesRemainingO == 0);
        if (allPlaced) {
            return (nextColor == Color.X) ? State.X_TO_MOVE : State.O_TO_MOVE;
        }
        return (nextColor == Color.X) ? State.X_TO_PUT : State.O_TO_PUT;
    }
}
