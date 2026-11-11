package tictactoe.domain;

/**
 * One place on the Board at a Coordinate. It holds at most one Piece.
 * Invariant I2: a Square holds at most one Piece; without a Piece it has Color EMPTY.
 */
final class Square {

    private final Coordinate coordinate;
    private Piece piece;

    Square(Coordinate coordinate) {
        this.coordinate = coordinate;
        this.piece = null;
    }

    /** Returns the Coordinate of this Square. */
    public Coordinate coordinate() {
        return coordinate;
    }

    /**
     * Returns the Color of the Piece on this Square, or EMPTY if the Square has no Piece.
     */
    public Color color() {
        return piece == null ? Color.EMPTY : piece.color();
    }

    /** Returns true when this Square has no Piece. */
    public boolean isEmpty() {
        return piece == null;
    }

    /**
     * Puts a Piece on this Square.
     * Package-private: only Board (and through it, TicTacToe) may call this.
     * Invariant I2: rejects a put on an occupied Square.
     */
    void put(Piece piece) {
        if (!isEmpty()) {
            throw new IllegalStateException("Square at " + coordinate + " is already occupied");
        }
        this.piece = piece;
    }

    /**
     * Removes and returns the Piece on this Square.
     * Package-private: only Board (and through it, TicTacToe) may call this.
     * Used by Board.move to lift a Piece before placing it elsewhere.
     * Invariant I2: rejects a remove on an empty Square.
     */
    Piece remove() {
        if (isEmpty()) {
            throw new IllegalStateException("Square at " + coordinate + " is empty");
        }
        Piece removed = this.piece;
        this.piece = null;
        return removed;
    }
}
