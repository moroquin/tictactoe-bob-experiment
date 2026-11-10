package tictactoe.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Three Coordinates in a row: a Row, a Column, the Diagonal or the Inverse Diagonal.
 * There are 8 Lines in total.
 */
public final class Line {

    private final List<Coordinate> coordinates;

    private Line(List<Coordinate> coordinates) {
        this.coordinates = Collections.unmodifiableList(new ArrayList<>(coordinates));
    }

    public List<Coordinate> coordinates() {
        return coordinates;
    }

    /** Returns the horizontal Line for the given row (1–3). */
    public static Line row(int n) {
        if (n < 1 || n > 3) throw new IllegalArgumentException("Row must be 1–3, got: " + n);
        return new Line(List.of(
                new Coordinate(n, 1),
                new Coordinate(n, 2),
                new Coordinate(n, 3)
        ));
    }

    /** Returns the vertical Line for the given column (1–3). */
    public static Line column(int n) {
        if (n < 1 || n > 3) throw new IllegalArgumentException("Column must be 1–3, got: " + n);
        return new Line(List.of(
                new Coordinate(1, n),
                new Coordinate(2, n),
                new Coordinate(3, n)
        ));
    }

    /** Returns the diagonal Line (1,1)–(2,2)–(3,3). */
    public static Line diagonal() {
        return new Line(List.of(
                new Coordinate(1, 1),
                new Coordinate(2, 2),
                new Coordinate(3, 3)
        ));
    }

    /** Returns the inverse diagonal Line (1,3)–(2,2)–(3,1). */
    public static Line inverseDiagonal() {
        return new Line(List.of(
                new Coordinate(1, 3),
                new Coordinate(2, 2),
                new Coordinate(3, 1)
        ));
    }

    /** Returns all 8 Lines: 3 rows, 3 columns, the diagonal, and the inverse diagonal. */
    public static List<Line> all() {
        List<Line> lines = new ArrayList<>(8);
        for (int i = 1; i <= 3; i++) lines.add(row(i));
        for (int i = 1; i <= 3; i++) lines.add(column(i));
        lines.add(diagonal());
        lines.add(inverseDiagonal());
        return Collections.unmodifiableList(lines);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Line other)) return false;
        return Objects.equals(coordinates, other.coordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinates);
    }
}
