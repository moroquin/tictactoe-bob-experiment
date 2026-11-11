package tictactoe.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoordinateTest {

    @Test
    void I1_validCoordinateIsCreated() {
        Coordinate c = new Coordinate(2, 3);
        assertEquals(2, c.row());
        assertEquals(3, c.column());
    }

    @Test
    void I1_rowBelowOneIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Coordinate(0, 1));
    }

    @Test
    void I1_rowAboveThreeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Coordinate(4, 1));
    }

    @Test
    void I1_columnBelowOneIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Coordinate(1, 0));
    }

    @Test
    void I1_columnAboveThreeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Coordinate(1, 4));
    }

    @Test
    void I1_allReturnsNineCoordinatesInReadingOrder() {
        List<Coordinate> all = Coordinate.all();
        assertEquals(9, all.size());
        // reading order: row 1–3, column 1–3 within each row
        int index = 0;
        for (int r = 1; r <= 3; r++) {
            for (int c = 1; c <= 3; c++) {
                assertEquals(new Coordinate(r, c), all.get(index++));
            }
        }
    }

    @Test
    void coordinatesWithSameRowAndColumnAreEqual() {
        assertEquals(new Coordinate(1, 2), new Coordinate(1, 2));
    }

    @Test
    void coordinatesWithDifferentRowAreNotEqual() {
        assertNotEquals(new Coordinate(1, 2), new Coordinate(2, 2));
    }

    @Test
    void coordinatesWithDifferentColumnAreNotEqual() {
        assertNotEquals(new Coordinate(1, 2), new Coordinate(1, 3));
    }

    @Test
    void equalCoordinatesHaveSameHashCode() {
        assertEquals(
                new Coordinate(3, 3).hashCode(),
                new Coordinate(3, 3).hashCode()
        );
    }
}
