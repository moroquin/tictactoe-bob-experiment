package tictactoe.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The row and the column of a Square, each from 1 to 3.
 * A value object: two Coordinates with the same row and column are equal.
 */
public final class Coordinate {

    private final int row;
    private final int column;

    /**
     * Creates a Coordinate. Row and column must each be from 1 to 3 (I1).
     *
     * @throws IllegalArgumentException if row or column is outside 1–3
     */
    public Coordinate(int row, int column) {
        if (row < 1 || row > 3) {
            throw new IllegalArgumentException("Row must be 1–3, got: " + row);
        }
        if (column < 1 || column > 3) {
            throw new IllegalArgumentException("Column must be 1–3, got: " + column);
        }
        this.row = row;
        this.column = column;
    }

    public int row() {
        return row;
    }

    public int column() {
        return column;
    }

    /** Returns all 9 Coordinates in reading order (row 1–3, column 1–3 within each row). */
    public static List<Coordinate> all() {
        List<Coordinate> list = new ArrayList<>(9);
        for (int r = 1; r <= 3; r++) {
            for (int c = 1; c <= 3; c++) {
                list.add(new Coordinate(r, c));
            }
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate other)) return false;
        return row == other.row && column == other.column;
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
