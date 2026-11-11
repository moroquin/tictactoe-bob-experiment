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
    void I1_newBoardHasAllSquaresEmpty() {
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
    void I2_colorAtReturnsColorAfterPut() {
        Coordinate c = new Coordinate(2, 2);
        board.put(c, new Piece(Color.X));
        assertEquals(Color.X, board.colorAt(c));
        assertFalse(board.isEmpty(c));
    }

    @Test
    void I6_hasWinFalseWhenNoPiecesPlaced() {
        assertFalse(board.hasWin(Color.X));
        assertFalse(board.hasWin(Color.O));
    }

    @Test
    void I6_hasWinTrueForCompleteRow() {
        board.put(new Coordinate(1, 1), new Piece(Color.X));
        board.put(new Coordinate(1, 2), new Piece(Color.X));
        board.put(new Coordinate(1, 3), new Piece(Color.X));
        assertTrue(board.hasWin(Color.X));
        assertFalse(board.hasWin(Color.O));
    }

    @Test
    void I6_hasWinTrueForCompleteColumn() {
        board.put(new Coordinate(1, 2), new Piece(Color.O));
        board.put(new Coordinate(2, 2), new Piece(Color.O));
        board.put(new Coordinate(3, 2), new Piece(Color.O));
        assertTrue(board.hasWin(Color.O));
    }

    @Test
    void I6_hasWinTrueForDiagonal() {
        board.put(new Coordinate(1, 1), new Piece(Color.X));
        board.put(new Coordinate(2, 2), new Piece(Color.X));
        board.put(new Coordinate(3, 3), new Piece(Color.X));
        assertTrue(board.hasWin(Color.X));
    }

    @Test
    void I6_hasWinTrueForInverseDiagonal() {
        board.put(new Coordinate(1, 3), new Piece(Color.O));
        board.put(new Coordinate(2, 2), new Piece(Color.O));
        board.put(new Coordinate(3, 1), new Piece(Color.O));
        assertTrue(board.hasWin(Color.O));
    }

    @Test
    void I6_hasWinFalseForIncompleteRow() {
        board.put(new Coordinate(1, 1), new Piece(Color.X));
        board.put(new Coordinate(1, 2), new Piece(Color.X));
        assertFalse(board.hasWin(Color.X));
    }

    @Test
    void I7_boardIsFullWhenAllSquaresOccupied() {
        // Fill the board with alternating X and O pieces
        Color[] colors = {Color.X, Color.O, Color.X, Color.O, Color.X, Color.O, Color.X, Color.O, Color.X};
        int i = 0;
        for (Coordinate c : Coordinate.all()) {
            board.put(c, new Piece(colors[i++]));
        }
        assertTrue(board.isFull());
    }
}
