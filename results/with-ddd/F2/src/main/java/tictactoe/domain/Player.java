package tictactoe.domain;

/**
 * One of the two participants. A Player owns a Color.
 * A Player chooses the Coordinate of the next Put; how it does so depends on the kind of Player.
 *
 * chooseCoordinate() returns the Coordinate this Player wants to put on, or null when the Player
 * defers to the caller (e.g. a Human Player whose Coordinate is read from input by the console).
 * No caller ever asks which kind of Player this is (D11).
 */
public abstract class Player {

    private final Color color;

    protected Player(Color color) {
        if (color == Color.EMPTY) {
            throw new IllegalArgumentException("A Player cannot have Color EMPTY");
        }
        this.color = color;
    }

    public Color color() {
        return color;
    }

    /**
     * Returns the Coordinate this Player wants to put on next, given the current game state,
     * or null when the Player defers (Human Player: the console reads the Coordinate instead).
     */
    public abstract Coordinate chooseCoordinate(TicTacToe game);
}
