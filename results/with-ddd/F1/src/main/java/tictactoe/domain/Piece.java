package tictactoe.domain;

/** What a Player puts on a Square. It has a Color. */
public final class Piece {

    private final Color color;

    public Piece(Color color) {
        if (color == Color.EMPTY) {
            throw new IllegalArgumentException("A Piece cannot have Color EMPTY");
        }
        this.color = color;
    }

    public Color color() {
        return color;
    }
}
