package tictactoe.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LineTest {

    @Test
    void rowLineContainsThreeCorrectCoordinates() {
        Line r2 = Line.row(2);
        List<Coordinate> coords = r2.coordinates();
        assertEquals(3, coords.size());
        assertEquals(new Coordinate(2, 1), coords.get(0));
        assertEquals(new Coordinate(2, 2), coords.get(1));
        assertEquals(new Coordinate(2, 3), coords.get(2));
    }

    @Test
    void columnLineContainsThreeCorrectCoordinates() {
        Line c3 = Line.column(3);
        List<Coordinate> coords = c3.coordinates();
        assertEquals(3, coords.size());
        assertEquals(new Coordinate(1, 3), coords.get(0));
        assertEquals(new Coordinate(2, 3), coords.get(1));
        assertEquals(new Coordinate(3, 3), coords.get(2));
    }

    @Test
    void diagonalContainsCorrectCoordinates() {
        Line d = Line.diagonal();
        List<Coordinate> coords = d.coordinates();
        assertEquals(new Coordinate(1, 1), coords.get(0));
        assertEquals(new Coordinate(2, 2), coords.get(1));
        assertEquals(new Coordinate(3, 3), coords.get(2));
    }

    @Test
    void inverseDiagonalContainsCorrectCoordinates() {
        Line id = Line.inverseDiagonal();
        List<Coordinate> coords = id.coordinates();
        assertEquals(new Coordinate(1, 3), coords.get(0));
        assertEquals(new Coordinate(2, 2), coords.get(1));
        assertEquals(new Coordinate(3, 1), coords.get(2));
    }

    @Test
    void allReturnsEightLines() {
        assertEquals(8, Line.all().size());
    }

    @Test
    void rowOutOfRangeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> Line.row(0));
        assertThrows(IllegalArgumentException.class, () -> Line.row(4));
    }

    @Test
    void columnOutOfRangeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> Line.column(0));
        assertThrows(IllegalArgumentException.class, () -> Line.column(4));
    }
}
