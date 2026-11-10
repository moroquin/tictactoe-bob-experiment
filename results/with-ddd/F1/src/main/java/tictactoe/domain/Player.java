package tictactoe.domain;

/** One of the two participants. A Player owns a Color. */
public final class Player {

    private final Color color;

    public Player(Color color) {
        if (color == Color.EMPTY) {
            throw new IllegalArgumentException("A Player cannot have Color EMPTY");
        }
        this.color = color;
    }

    public Color color() {
        return color;
    }
}
