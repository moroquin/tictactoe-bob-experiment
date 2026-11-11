package tictactoe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the game rules implemented in Board.
 */
class BoardTest {

    // -------------------------------------------------------------------------
    // Initial state
    // -------------------------------------------------------------------------

    @Test
    void newBoardIsEmpty() {
        Board board = new Board();
        for (int r = 1; r <= 3; r++) {
            for (int c = 1; c <= 3; c++) {
                assertNull(board.at(r, c), "Expected empty at " + r + "," + c);
            }
        }
    }

    @Test
    void newBoardStateIsInProgress() {
        assertEquals(State.IN_PROGRESS, new Board().state());
    }

    @Test
    void xGoesFirst() {
        assertEquals(Color.X, new Board().currentPlayer());
    }

    // -------------------------------------------------------------------------
    // Turn alternation
    // -------------------------------------------------------------------------

    @Test
    void playersAlternate() {
        Board board = new Board().place(1, 1); // X placed
        assertEquals(Color.O, board.currentPlayer());
        board = board.place(2, 2); // O placed
        assertEquals(Color.X, board.currentPlayer());
    }

    @Test
    void placedPieceIsStoredWithCorrectColor() {
        Board board = new Board().place(2, 3); // X
        assertEquals(Color.X, board.at(2, 3));
    }

    // -------------------------------------------------------------------------
    // Invalid moves are rejected
    // -------------------------------------------------------------------------

    @Test
    void cannotPlaceOutsideBoard_rowTooLow() {
        assertThrows(IllegalArgumentException.class, () -> new Board().place(0, 1));
    }

    @Test
    void cannotPlaceOutsideBoard_rowTooHigh() {
        assertThrows(IllegalArgumentException.class, () -> new Board().place(4, 1));
    }

    @Test
    void cannotPlaceOutsideBoard_colTooLow() {
        assertThrows(IllegalArgumentException.class, () -> new Board().place(1, 0));
    }

    @Test
    void cannotPlaceOutsideBoard_colTooHigh() {
        assertThrows(IllegalArgumentException.class, () -> new Board().place(1, 4));
    }

    @Test
    void cannotPlaceOnOccupiedSquare() {
        Board board = new Board().place(1, 1); // X
        assertThrows(IllegalArgumentException.class, () -> board.place(1, 1));
    }

    @Test
    void cannotPlaceAfterGameOver() {
        Board over = drawBoard();
        assertEquals(State.DRAW, over.state());
        assertThrows(IllegalArgumentException.class, () -> over.place(1, 1));
    }

    // -------------------------------------------------------------------------
    // Win detection — rows
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({"1", "2", "3"})
    void xWinsOnRow(int row) {
        // X fills the given row; O fills the previous row (row 1 if row>1, else row 2)
        int oRow = row == 1 ? 2 : 1;
        Board board = new Board();
        board = board.place(row,  1); // X
        board = board.place(oRow, 1); // O
        board = board.place(row,  2); // X
        board = board.place(oRow, 2); // O
        board = board.place(row,  3); // X — wins
        assertEquals(State.X_WINS, board.state());
    }

    @ParameterizedTest
    @CsvSource({"1", "2", "3"})
    void oWinsOnRow(int row) {
        // O fills the given row; X uses scattered squares that don't form a line
        // X plays column 1 of two different rows (not completing a row/col/diag)
        int xRow1 = row == 1 ? 2 : 1;
        int xRow2 = row == 3 ? 2 : 3;
        Board board = new Board();
        board = board.place(xRow1, 1); // X move 1 — col 1 of xRow1
        board = board.place(row,   1); // O move 2
        board = board.place(xRow2, 2); // X move 3 — different row+col, no line
        board = board.place(row,   2); // O move 4
        board = board.place(xRow1, 3); // X move 5 — no line formed
        board = board.place(row,   3); // O move 6 — wins
        assertEquals(State.O_WINS, board.state());
    }

    // -------------------------------------------------------------------------
    // Win detection — columns
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({"1", "2", "3"})
    void xWinsOnColumn(int col) {
        int oCol = col == 1 ? 2 : 1;
        Board board = new Board();
        board = board.place(1, col);  // X
        board = board.place(1, oCol); // O
        board = board.place(2, col);  // X
        board = board.place(2, oCol); // O
        board = board.place(3, col);  // X — wins
        assertEquals(State.X_WINS, board.state());
    }

    // -------------------------------------------------------------------------
    // Win detection — diagonals
    // -------------------------------------------------------------------------

    @Test
    void xWinsOnMainDiagonal() {
        // X: (1,1)(2,2)(3,3)   O: (1,2)(2,3)
        Board board = new Board();
        board = board.place(1, 1); // X
        board = board.place(1, 2); // O
        board = board.place(2, 2); // X
        board = board.place(2, 3); // O
        board = board.place(3, 3); // X — wins
        assertEquals(State.X_WINS, board.state());
    }

    @Test
    void xWinsOnAntiDiagonal() {
        // X: (1,3)(2,2)(3,1)   O: (1,1)(2,1)
        Board board = new Board();
        board = board.place(1, 3); // X
        board = board.place(1, 1); // O
        board = board.place(2, 2); // X
        board = board.place(2, 1); // O
        board = board.place(3, 1); // X — wins
        assertEquals(State.X_WINS, board.state());
    }

    // -------------------------------------------------------------------------
    // Draw
    // -------------------------------------------------------------------------

    @Test
    void drawWhenAllSquaresFilledWithNoWinner() {
        assertEquals(State.DRAW, drawBoard().state());
    }

    // -------------------------------------------------------------------------
    // Win is checked before draw (last move wins when board becomes full)
    // -------------------------------------------------------------------------

    @Test
    void winCheckedBeforeDraw() {
        // 9th move fills the board and wins for X (row 3):
        //   O O X
        //   O O X
        //   X X X  <- X wins row 3 on final square; board is full
        Board board = new Board();
        board = board.place(1, 3); // X  move 1
        board = board.place(1, 1); // O  move 2
        board = board.place(2, 3); // X  move 3
        board = board.place(2, 1); // O  move 4
        board = board.place(3, 1); // X  move 5
        board = board.place(1, 2); // O  move 6
        board = board.place(3, 2); // X  move 7
        board = board.place(2, 2); // O  move 8
        board = board.place(3, 3); // X  move 9 — row 3 complete, board full
        assertEquals(State.X_WINS, board.state());
    }

    // -------------------------------------------------------------------------
    // Immutability
    // -------------------------------------------------------------------------

    @Test
    void placeDoesNotMutateOriginalBoard() {
        Board original = new Board();
        original.place(2, 2);
        assertNull(original.at(2, 2), "Original board must not be mutated");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a drawn board with this arrangement (no winner):
     *   X O X
     *   X X O
     *   O X O
     */
    private static Board drawBoard() {
        Board b = new Board();
        b = b.place(1, 1); // X
        b = b.place(1, 2); // O
        b = b.place(1, 3); // X
        b = b.place(2, 3); // O
        b = b.place(2, 1); // X
        b = b.place(3, 1); // O
        b = b.place(2, 2); // X
        b = b.place(3, 3); // O
        b = b.place(3, 2); // X
        return b;
    }
}
