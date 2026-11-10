package tictactoe.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoordinateTest {

    @Test
    void I1_rowAndColumnAreStoredCorrectly() {
        Coordinate c = new Coordinate(2, 3);
        assertEquals(2, c.row());
        assertEquals(3, c.column());
    }

    @Test
    void I1_rowBelowOneThrwosException() {
        assertThrows(IllegalArgumentException.class, () -> new Coordinate(0, 1));
    }

    @Test
    void I1_rowAboveThreeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Coordinate(4, 1));
    }

    @Test
    void I1_columnBelowOneThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Coordinate(1, 0));
    }

    @Test
    void I1_columnAboveThreeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Coordinate(1, 4));
    }

    @Test
    void I1_allReturnsNineCoordinatesInReadingOrder() {
        List<Coordinate> all = Coordinate.all();
        assertEquals(9, all.size());
        // First is (1,1), last is (3,3), reading order
        assertEquals(new Coordinate(1, 1), all.get(0));
        assertEquals(new Coordinate(1, 2), all.get(1));
        assertEquals(new Coordinate(1, 3), all.get(2));
        assertEquals(new Coordinate(2, 1), all.get(3));
        assertEquals(new Coordinate(3, 3), all.get(8));
    }

    @Test
    void equalCoordinatesAreEqual() {
        assertEquals(new Coordinate(2, 2), new Coordinate(2, 2));
    }

    @Test
    void differentCoordinatesAreNotEqual() {
        assertNotEquals(new Coordinate(1, 2), new Coordinate(2, 1));
    }

    @Test
    void equalCoordinatesHaveSameHashCode() {
        assertEquals(new Coordinate(3, 1).hashCode(), new Coordinate(3, 1).hashCode());
    }
}
