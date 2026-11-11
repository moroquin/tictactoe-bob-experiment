package tictactoe.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * The 3×3 set of Squares.
 * Invariant I1: the Board always has exactly 9 Squares.
 */
final class Board {

    private final Map<Coordinate, Square> squares = new HashMap<>(9);

    Board() {
        for (Coordinate c : Coordinate.all()) {
            squares.put(c, new Square(c));
        }
    }

    /** Returns the Color at the given Coordinate (EMPTY if no Piece is there). */
    public Color colorAt(Coordinate coordinate) {
        return squareAt(coordinate).color();
    }

    /** Returns true if the Square at the given Coordinate is empty. */
    public boolean isEmpty(Coordinate coordinate) {
        return squareAt(coordinate).isEmpty();
    }

    /** Returns true if all 9 Squares are occupied. */
    public boolean isFull() {
        return squares.values().stream().noneMatch(Square::isEmpty);
    }

    /**
     * Returns true if the given Color has three Pieces on any one Line.
     * Invariant I6: checks all 8 Lines.
     */
    public boolean hasWin(Color color) {
        for (Line line : Line.all()) {
            boolean win = true;
            for (Coordinate c : line.coordinates()) {
                if (colorAt(c) != color) {
                    win = false;
                    break;
                }
            }
            if (win) return true;
        }
        return false;
    }

    /**
     * Puts a Piece on the Square at the given Coordinate.
     * Package-private: only TicTacToe may call this.
     */
    void put(Coordinate coordinate, Piece piece) {
        squareAt(coordinate).put(piece);
    }

    /**
     * Moves the Piece from the Square at {@code from} to the Square at {@code to}.
     * Package-private: only TicTacToe may call this.
     * CR-2: used in the three-pieces variant (D21).
     */
    void move(Coordinate from, Coordinate to) {
        Piece piece = squareAt(from).remove();
        squareAt(to).put(piece);
    }

    private Square squareAt(Coordinate coordinate) {
        Square square = squares.get(coordinate);
        if (square == null) {
            throw new IllegalArgumentException("No square at " + coordinate);
        }
        return square;
    }
}
