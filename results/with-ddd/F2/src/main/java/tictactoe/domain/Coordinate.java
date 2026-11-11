package tictactoe.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The row and column of a Square, each from 1 to 3.
 * A value object: two Coordinates with the same row and column are equal.
 * Invariant I1: a Coordinate outside 1–3 cannot be created.
 */
public final class Coordinate {

    private final int row;
    private final int column;

    /**
     * Creates a Coordinate.
     *
     * @throws IllegalArgumentException if row or column is outside 1–3
     */
    public Coordinate(int row, int column) {
        if (row < 1 || row > 3 || column < 1 || column > 3) {
            throw new IllegalArgumentException(
                    "Row and column must be from 1 to 3, got: row=" + row + ", column=" + column);
        }
        this.row = row;
        this.column = column;
    }

    /** Returns the row (1–3). */
    public int row() {
        return row;
    }

    /** Returns the column (1–3). */
    public int column() {
        return column;
    }

    /**
     * Returns all 9 Coordinates in reading order (row 1 to 3, column 1 to 3 within each row).
     */
    public static List<Coordinate> all() {
        List<Coordinate> list = new ArrayList<>(9);
        for (int r = 1; r <= 3; r++) {
            for (int c = 1; c <= 3; c++) {
                list.add(new Coordinate(r, c));
            }
        }
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate that)) return false;
        return row == that.row && column == that.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    @Override
    public String toString() {
        return "(" + row + "," + column + ")";
    }
}
