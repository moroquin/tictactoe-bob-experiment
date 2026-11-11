package tictactoe.domain;

/**
 * A Piece placed by a Player on a Square. It has a Color.
 * Its position is the Square that holds it, not a field of Piece.
 */
public final class Piece {

    private final Color color;

    public Piece(Color color) {
        if (color == Color.EMPTY) {
            throw new IllegalArgumentException("A Piece cannot have Color EMPTY");
        }
        this.color = color;
    }

    /** Returns the Color of this Piece (X or O). */
    public Color color() {
        return color;
    }
}
