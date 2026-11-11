package tictactoe.domain;

import java.util.List;

/**
 * Three Coordinates in a row: a Row, a Column, the Diagonal or the Inverse Diagonal.
 * There are 8 Lines in total. A value object.
 */
public final class Line {

    private final List<Coordinate> coordinates;

    private Line(List<Coordinate> coordinates) {
        this.coordinates = List.copyOf(coordinates);
    }

    /** Returns the three Coordinates of this Line. */
    public List<Coordinate> coordinates() {
        return coordinates;
    }

    /** Returns the horizontal Line for row n (1–3). */
    public static Line row(int n) {
        return new Line(List.of(
                new Coordinate(n, 1),
                new Coordinate(n, 2),
                new Coordinate(n, 3)
        ));
    }

    /** Returns the vertical Line for column n (1–3). */
    public static Line column(int n) {
        return new Line(List.of(
                new Coordinate(1, n),
                new Coordinate(2, n),
                new Coordinate(3, n)
        ));
    }

    /** Returns the Diagonal (1,1)–(2,2)–(3,3). */
    public static Line diagonal() {
        return new Line(List.of(
                new Coordinate(1, 1),
                new Coordinate(2, 2),
                new Coordinate(3, 3)
        ));
    }

    /** Returns the Inverse Diagonal (1,3)–(2,2)–(3,1). */
    public static Line inverseDiagonal() {
        return new Line(List.of(
                new Coordinate(1, 3),
                new Coordinate(2, 2),
                new Coordinate(3, 1)
        ));
    }

    /**
     * Returns all 8 Lines: 3 rows, 3 columns, 1 diagonal, 1 inverse diagonal.
     */
    public static List<Line> all() {
        return List.of(
                row(1), row(2), row(3),
                column(1), column(2), column(3),
                diagonal(), inverseDiagonal()
        );
    }
}
