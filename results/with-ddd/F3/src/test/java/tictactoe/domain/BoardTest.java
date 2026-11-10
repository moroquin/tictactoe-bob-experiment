package tictactoe.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    void I1_newBoardHasNineEmptySquares() {
        for (Coordinate c : Coordinate.all()) {
            assertEquals(Color.EMPTY, board.colorAt(c));
            assertTrue(board.isEmpty(c));
        }
    }

    @Test
    void I1_newBoardIsNotFull() {
        assertFalse(board.isFull());
    }

    @Test
    void I2_afterPutColorAtCoordinateMatchesPieceColor() {
        Coordinate c = new Coordinate(2, 2);
        board.put(c, new Piece(Color.X));
        assertEquals(Color.X, board.colorAt(c));
        assertFalse(board.isEmpty(c));
    }

    @Test
    void I6_hasWinDetectsRowWin() {
        // fill row 1 with X
        board.put(new Coordinate(1, 1), new Piece(Color.X));
        board.put(new Coordinate(1, 2), new Piece(Color.X));
        board.put(new Coordinate(1, 3), new Piece(Color.X));
        assertTrue(board.hasWin(Color.X));
        assertFalse(board.hasWin(Color.O));
    }

    @Test
    void I6_hasWinDetectsColumnWin() {
        board.put(new Coordinate(1, 2), new Piece(Color.O));
        board.put(new Coordinate(2, 2), new Piece(Color.O));
        board.put(new Coordinate(3, 2), new Piece(Color.O));
        assertTrue(board.hasWin(Color.O));
    }

    @Test
    void I6_hasWinDetectsDiagonalWin() {
        board.put(new Coordinate(1, 1), new Piece(Color.X));
        board.put(new Coordinate(2, 2), new Piece(Color.X));
        board.put(new Coordinate(3, 3), new Piece(Color.X));
        assertTrue(board.hasWin(Color.X));
    }

    @Test
    void I6_hasWinDetectsInverseDiagonalWin() {
        board.put(new Coordinate(1, 3), new Piece(Color.O));
        board.put(new Coordinate(2, 2), new Piece(Color.O));
        board.put(new Coordinate(3, 1), new Piece(Color.O));
        assertTrue(board.hasWin(Color.O));
    }

    @Test
    void I6_hasWinReturnsFalseForNoWin() {
        board.put(new Coordinate(1, 1), new Piece(Color.X));
        board.put(new Coordinate(2, 2), new Piece(Color.X));
        assertFalse(board.hasWin(Color.X));
    }

    @Test
    void I6_hasWinReturnsFalseForColorEmpty() {
        assertFalse(board.hasWin(Color.EMPTY));
    }

    @Test
    void I7_boardIsFullWhenAllSquaresOccupied() {
        Color[] pattern = {
                Color.X, Color.O, Color.X,
                Color.X, Color.O, Color.O,
                Color.O, Color.X, Color.X
        };
        int i = 0;
        for (Coordinate c : Coordinate.all()) {
            board.put(c, new Piece(pattern[i++]));
        }
        assertTrue(board.isFull());
    }
}
