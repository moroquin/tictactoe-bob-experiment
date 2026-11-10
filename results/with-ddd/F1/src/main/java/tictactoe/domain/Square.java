package tictactoe.domain;

/**
 * One place on the Board, at one Coordinate. It holds at most one Piece.
 * put() is package-private: only Board calls it (I10).
 */
public final class Square {

    private final Coordinate coordinate;
    private Piece piece;  // null means empty

    Square(Coordinate coordinate) {
        this.coordinate = coordinate;
        this.piece = null;
    }

    public Coordinate coordinate() {
        return coordinate;
    }

    /** Returns the Color of the Piece on this Square, or Color.EMPTY if empty (I2, D3). */
    public Color color() {
        return piece == null ? Color.EMPTY : piece.color();
    }

    public boolean isEmpty() {
        return piece == null;
    }

    /**
     * Puts a Piece on this Square.
     * Package-private: only Board may call this (I10).
     *
     * @throws IllegalStateException if the Square is already occupied (I2)
     */
    void put(Piece p) {
        if (!isEmpty()) {
            throw new IllegalStateException("Square " + coordinate + " is already occupied");
        }
        this.piece = p;
    }
}
