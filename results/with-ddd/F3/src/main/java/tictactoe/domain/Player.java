package tictactoe.domain;

/**
 * One of the two participants. A Player owns a Color (X or O).
 * A Player can be a Human Player or a PC Player.
 * Invariant: no class asks which kind of Player this is.
 */
public abstract class Player {

    private final Color color;

    protected Player(Color color) {
        if (color == Color.EMPTY) {
            throw new IllegalArgumentException("A Player cannot have Color EMPTY");
        }
        this.color = color;
    }

    /** Returns the Color of this Player (X or O). */
    public Color color() {
        return color;
    }

    /**
     * Chooses the Coordinate for the next Put.
     * A Human Player delegates to its CoordinateSupplier.
     * A PC Player computes the Coordinate from the game state.
     *
     * @param game the current game (read-only via its public methods)
     * @return the Coordinate chosen for the next Put; never null
     */
    public abstract Coordinate chooseCoordinate(TicTacToe game);
}
