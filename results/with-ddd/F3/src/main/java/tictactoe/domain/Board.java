package tictactoe.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * The 3×3 set of Squares. Answers what is on each Square and whether
 * there is a Win or the Board is full.
 * put() is package-private: only TicTacToe calls it (I10).
 */
public final class Board {

    private final Map<Coordinate, Square> squares;

    Board() {
        squares = new HashMap<>(9);
        for (Coordinate c : Coordinate.all()) {
            squares.put(c, new Square(c));
        }
    }

    public Color colorAt(Coordinate coordinate) {
        return squareAt(coordinate).color();
    }

    public boolean isEmpty(Coordinate coordinate) {
        return squareAt(coordinate).isEmpty();
    }

    /** Returns true when all 9 Squares are occupied. */
    public boolean isFull() {
        return squares.values().stream().noneMatch(Square::isEmpty);
    }

    /**
     * Returns true when the given Color has three Pieces on one Line (I6).
     * EMPTY is never a win.
     */
    public boolean hasWin(Color color) {
        if (color == Color.EMPTY) return false;
        return Line.all().stream().anyMatch(line ->
                line.coordinates().stream()
                        .allMatch(c -> colorAt(c) == color)
        );
    }

    /**
     * Puts a Piece on the Square at the given Coordinate.
     * Package-private: only TicTacToe may call this (I10).
     */
    void put(Coordinate coordinate, Piece piece) {
        squareAt(coordinate).put(piece);
    }

    /**
     * Moves the Piece from the Square at {@code from} to the Square at {@code to}.
     * Package-private: only ThreePiecesTicTacToe may call this (I10, D15).
     * The from-Square must be occupied and the to-Square must be empty.
     */
    void move(Coordinate from, Coordinate to) {
        Piece piece = squareAt(from).remove();
        squareAt(to).put(piece);
    }

    private Square squareAt(Coordinate coordinate) {
        return squares.get(coordinate);
    }
}
