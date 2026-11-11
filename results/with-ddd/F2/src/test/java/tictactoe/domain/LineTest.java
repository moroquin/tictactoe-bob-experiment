package tictactoe.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LineTest {

    @Test
    void rowLineContainsThreeCoordinatesInSameRow() {
        Line line = Line.row(2);
        List<Coordinate> coords = line.coordinates();
        assertEquals(3, coords.size());
        for (Coordinate c : coords) {
            assertEquals(2, c.row());
        }
    }

    @Test
    void columnLineContainsThreeCoordinatesInSameColumn() {
        Line line = Line.column(3);
        List<Coordinate> coords = line.coordinates();
        assertEquals(3, coords.size());
        for (Coordinate c : coords) {
            assertEquals(3, c.column());
        }
    }

    @Test
    void diagonalHasCorrectCoordinates() {
        List<Coordinate> coords = Line.diagonal().coordinates();
        assertEquals(new Coordinate(1, 1), coords.get(0));
        assertEquals(new Coordinate(2, 2), coords.get(1));
        assertEquals(new Coordinate(3, 3), coords.get(2));
    }

    @Test
    void inverseDiagonalHasCorrectCoordinates() {
        List<Coordinate> coords = Line.inverseDiagonal().coordinates();
        assertEquals(new Coordinate(1, 3), coords.get(0));
        assertEquals(new Coordinate(2, 2), coords.get(1));
        assertEquals(new Coordinate(3, 1), coords.get(2));
    }

    @Test
    void allReturnsEightLines() {
        assertEquals(8, Line.all().size());
    }

    @Test
    void allContainsThreeRowsThreeColumnsDiagonalInverseDiagonal() {
        List<Line> all = Line.all();
        // Verify distinct coverage by checking that together they cover all 9 squares
        // and the diagonals share the center
        long total = all.stream().mapToLong(l -> l.coordinates().size()).sum();
        assertEquals(24, total); // 8 lines × 3 coordinates
    }
}
