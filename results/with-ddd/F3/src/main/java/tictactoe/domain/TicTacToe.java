package tictactoe.domain;

/**
 * The aggregate root: the game itself. One Board, one Round, two Players.
 * Invariant I10: only TicTacToe changes a game; all mutators on Board, Square
 * and Round are package-private and only called from here.
 *
 * CR-2: adds a pieceLimit constructor, move(from, to), piecesLeft(color) (D20, D21, D25).
 */
public final class TicTacToe {

    private static final int UNLIMITED = 0;

    private final Board board;
    private final Round round;
    private State state;

    /** Pieces still available to Put, per color. MAX_VALUE means unlimited. */
    private int piecesLeftX;
    private int piecesLeftO;

    /**
     * Creates a new TicTacToe game with no piece limit (standard variant). X always starts.
     *
     * @param playerX the Player who plays X
     * @param playerO the Player who plays O
     */
    public TicTacToe(Player playerX, Player playerO) {
        this(playerX, playerO, UNLIMITED);
    }

    /**
     * Creates a new TicTacToe game. X always starts.
     *
     * @param playerX    the Player who plays X
     * @param playerO    the Player who plays O
     * @param pieceLimit the maximum number of Puts each Player may make;
     *                   0 means unlimited (standard variant).
     *                   With pieceLimit > 0 (three-pieces variant) the game transitions
     *                   to the move phase once both Players have placed all their Pieces.
     */
    public TicTacToe(Player playerX, Player playerO, int pieceLimit) {
        if (playerX.color() != Color.X) {
            throw new IllegalArgumentException("playerX must have Color X");
        }
        if (playerO.color() != Color.O) {
            throw new IllegalArgumentException("playerO must have Color O");
        }
        this.board = new Board();
        this.round = new Round(playerX, playerO);
        this.state = State.X_TO_PUT;
        this.piecesLeftX = (pieceLimit == UNLIMITED) ? Integer.MAX_VALUE : pieceLimit;
        this.piecesLeftO = (pieceLimit == UNLIMITED) ? Integer.MAX_VALUE : pieceLimit;
    }

    /**
     * Puts a Piece of the current Player's Color on the Square at the given Coordinate.
     * Returns true when the put succeeds; returns false and changes nothing when
     * the Square is already occupied (I3), the game is already over (I8), or
     * the current Player has no Pieces left to Put (I12).
     */
    public boolean put(Coordinate coordinate) {
        if (state.isOver() || state.isMoving()) {
            return false; // I8 / I12: no put after the game ends or in the move phase
        }
        Color color = round.current().color();
        int piecesLeft = (color == Color.X) ? piecesLeftX : piecesLeftO;
        if (piecesLeft <= 0) {
            return false; // I12: no pieces left to put
        }
        if (!board.isEmpty(coordinate)) {
            return false; // I3: reject put on occupied square
        }
        board.put(coordinate, new Piece(color)); // I5: the current Player's Color
        if (color == Color.X) piecesLeftX--; else piecesLeftO--;

        if (board.hasWin(color)) {              // I6: check win first
            state = (color == Color.X) ? State.X_WINS : State.O_WINS;
        } else if (board.isFull()) {            // I7: draw if full and no win
            state = State.DRAW;
        } else {
            round.next();                       // I4: alternate Players
            Color nextColor = round.current().color();
            int nextPiecesLeft = (nextColor == Color.X) ? piecesLeftX : piecesLeftO;
            if (nextPiecesLeft <= 0) {
                // The next player has no pieces left to put — check if we enter move phase
                // (i.e., the other player also has none left)
                int prevPiecesLeft = (color == Color.X) ? piecesLeftX : piecesLeftO;
                if (prevPiecesLeft <= 0) {
                    // Both players are done putting — enter move phase (I12)
                    state = (nextColor == Color.X) ? State.X_TO_MOVE : State.O_TO_MOVE;
                } else {
                    // Next player has no pieces, but the previous one still might —
                    // this cannot happen when pieceLimit is the same for both; stay in put phase.
                    state = (nextColor == Color.X) ? State.X_TO_PUT : State.O_TO_PUT;
                }
            } else {
                state = (nextColor == Color.X) ? State.X_TO_PUT : State.O_TO_PUT;
            }
        }
        return true;
    }

    /**
     * Moves a Piece of the current Player's Color from {@code from} to the empty Square at {@code to}.
     * Valid only in the move phase (X_TO_MOVE or O_TO_MOVE).
     * Returns true when the move succeeds; returns false and changes nothing when:
     * - the game is not in the move phase (I8),
     * - the origin does not hold a Piece of the current Player's Color (I11),
     * - the target is not empty (I11).
     * CR-2 (D21).
     */
    public boolean move(Coordinate from, Coordinate to) {
        if (!state.isMoving()) {
            return false; // I8: no move outside move phase
        }
        Color color = round.current().color();
        if (board.colorAt(from) != color) {
            return false; // I11: origin must hold a piece of the current player
        }
        if (!board.isEmpty(to)) {
            return false; // I11: target must be empty
        }
        board.move(from, to);                   // package-private; only TicTacToe calls it
        if (board.hasWin(color)) {              // I6: check win after every move
            state = (color == Color.X) ? State.X_WINS : State.O_WINS;
        } else {
            round.next();                       // I4: alternate Players
            Color nextColor = round.current().color();
            state = (nextColor == Color.X) ? State.X_TO_MOVE : State.O_TO_MOVE;
        }
        return true;
    }

    /** Returns the current State of the game. */
    public State state() {
        return state;
    }

    /** Returns the Player who acts next (only meaningful when the game is not over). */
    public Player currentPlayer() {
        return round.current();
    }

    /** Returns the Color at the given Coordinate (EMPTY if the Square is empty). */
    public Color colorAt(Coordinate coordinate) {
        return board.colorAt(coordinate);
    }

    /**
     * Returns the number of Puts the given Color may still make.
     * Returns {@link Integer#MAX_VALUE} in the standard variant (no piece limit).
     * CR-2 (D25).
     */
    public int piecesLeft(Color color) {
        if (color == Color.X) return piecesLeftX;
        if (color == Color.O) return piecesLeftO;
        throw new IllegalArgumentException("piecesLeft is not defined for EMPTY");
    }
}
