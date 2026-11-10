package tictactoe.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SquareTest {

    @Test
    void I2_newSquareIsEmpty() {
        Square square = new Square(new Coordinate(1, 1));
        assertTrue(square.isEmpty());
        assertEquals(Color.EMPTY, square.color());
    }

    @Test
    void I2_squareAfterPutHasCorrectColor() {
        Square square = new Square(new Coordinate(2, 2));
        square.put(new Piece(Color.X));
        assertFalse(square.isEmpty());
        assertEquals(Color.X, square.color());
    }

    @Test
    void I2_putOnOccupiedSquareThrowsException() {
        Square square = new Square(new Coordinate(3, 3));
        square.put(new Piece(Color.O));
        assertThrows(IllegalStateException.class, () -> square.put(new Piece(Color.X)));
    }

    @Test
    void squareReturnsItsCoordinate() {
        Coordinate c = new Coordinate(1, 3);
        Square square = new Square(c);
        assertEquals(c, square.coordinate());
    }
}
