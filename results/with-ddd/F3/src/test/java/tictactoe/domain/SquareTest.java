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
    void I2_squareWithPieceIsNotEmpty() {
        Square square = new Square(new Coordinate(1, 1));
        square.put(new Piece(Color.X));
        assertFalse(square.isEmpty());
    }

    @Test
    void I2_squareReturnsColorOfPiece() {
        Square square = new Square(new Coordinate(2, 2));
        square.put(new Piece(Color.O));
        assertEquals(Color.O, square.color());
    }

    @Test
    void I2_putOnOccupiedSquareThrows() {
        Square square = new Square(new Coordinate(1, 1));
        square.put(new Piece(Color.X));
        assertThrows(IllegalStateException.class, () -> square.put(new Piece(Color.O)));
    }

    @Test
    void squareReturnsItsCoordinate() {
        Coordinate coord = new Coordinate(3, 2);
        Square square = new Square(coord);
        assertEquals(coord, square.coordinate());
    }
}
