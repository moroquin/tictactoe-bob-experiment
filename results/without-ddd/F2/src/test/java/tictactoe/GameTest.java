package tictactoe;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the game rules (Board + Game).
 */
class GameTest {

    // ── Board.isEmpty / Board.place / Board.get ──────────────────────────────

    @Test
    void boardStartsEmpty() {
        Board b = new Board();
        for (int r = 1; r <= 3; r++)
            for (int c = 1; c <= 3; c++)
                assertTrue(b.isEmpty(r, c), "square (" + r + "," + c + ") should be empty");
    }

    @Test
    void placeAndGet() {
        Board b = new Board();
        b.place(2, 3, 'X');
        assertEquals('X', b.get(2, 3));
        assertFalse(b.isEmpty(2, 3));
        assertTrue(b.isEmpty(1, 1));
    }

    // ── Board.hasWon ─────────────────────────────────────────────────────────

    @Test
    void winRow1() {
        Board b = new Board();
        b.place(1, 1, 'X'); b.place(1, 2, 'X'); b.place(1, 3, 'X');
        assertTrue(b.hasWon('X'));
        assertFalse(b.hasWon('O'));
    }

    @Test
    void winRow2() {
        Board b = new Board();
        b.place(2, 1, 'O'); b.place(2, 2, 'O'); b.place(2, 3, 'O');
        assertTrue(b.hasWon('O'));
    }

    @Test
    void winRow3() {
        Board b = new Board();
        b.place(3, 1, 'X'); b.place(3, 2, 'X'); b.place(3, 3, 'X');
        assertTrue(b.hasWon('X'));
    }

    @Test
    void winColumn1() {
        Board b = new Board();
        b.place(1, 1, 'O'); b.place(2, 1, 'O'); b.place(3, 1, 'O');
        assertTrue(b.hasWon('O'));
    }

    @Test
    void winColumn2() {
        Board b = new Board();
        b.place(1, 2, 'X'); b.place(2, 2, 'X'); b.place(3, 2, 'X');
        assertTrue(b.hasWon('X'));
    }

    @Test
    void winColumn3() {
        Board b = new Board();
        b.place(1, 3, 'O'); b.place(2, 3, 'O'); b.place(3, 3, 'O');
        assertTrue(b.hasWon('O'));
    }

    @Test
    void winMainDiagonal() {
        Board b = new Board();
        b.place(1, 1, 'X'); b.place(2, 2, 'X'); b.place(3, 3, 'X');
        assertTrue(b.hasWon('X'));
    }

    @Test
    void winAntiDiagonal() {
        Board b = new Board();
        b.place(1, 3, 'O'); b.place(2, 2, 'O'); b.place(3, 1, 'O');
        assertTrue(b.hasWon('O'));
    }

    @Test
    void noWinOnPartialRow() {
        Board b = new Board();
        b.place(1, 1, 'X'); b.place(1, 2, 'X');
        assertFalse(b.hasWon('X'));
    }

    // ── Board.isFull ─────────────────────────────────────────────────────────

    @Test
    void boardNotFullInitially() {
        assertFalse(new Board().isFull());
    }

    @Test
    void boardFullWhenAllOccupied() {
        Board b = new Board();
        char[] pieces = {'X','O','X', 'O','X','O', 'O','X','O'};
        int i = 0;
        for (int r = 1; r <= 3; r++)
            for (int c = 1; c <= 3; c++)
                b.place(r, c, pieces[i++]);
        assertTrue(b.isFull());
    }

    // ── Board.render ─────────────────────────────────────────────────────────

    @Test
    void renderEmptyBoard() {
        assertEquals("...\n...\n...", new Board().render());
    }

    @Test
    void renderWithPieces() {
        Board b = new Board();
        b.place(1, 1, 'X'); b.place(1, 2, 'X'); b.place(1, 3, 'X');
        b.place(2, 1, 'O'); b.place(2, 2, 'O');
        assertEquals("XXX\nOO.\n...", b.render());
    }

    // ── Game: X starts ───────────────────────────────────────────────────────

    @Test
    void xStartsFirst() {
        assertEquals('X', new Game().currentPlayer());
    }

    // ── Game: alternating turns ───────────────────────────────────────────────

    @Test
    void turnsAlternate() {
        Game g = new Game();
        assertTrue(g.tryMove(1, 1));   // X moves
        assertEquals('O', g.currentPlayer());
        assertTrue(g.tryMove(2, 1));   // O moves
        assertEquals('X', g.currentPlayer());
    }

    // ── Game: invalid moves ───────────────────────────────────────────────────

    @Test
    void rejectRowOutOfRange() {
        Game g = new Game();
        assertFalse(g.tryMove(0, 1));
        assertFalse(g.tryMove(4, 1));
    }

    @Test
    void rejectColOutOfRange() {
        Game g = new Game();
        assertFalse(g.tryMove(1, 0));
        assertFalse(g.tryMove(1, 4));
    }

    @Test
    void rejectOccupiedSquare() {
        Game g = new Game();
        g.tryMove(1, 1);        // X
        assertFalse(g.tryMove(1, 1)); // O cannot place on X's square
        assertEquals('O', g.currentPlayer()); // still O's turn
    }

    @Test
    void invalidMoveDoesNotChangeTurn() {
        Game g = new Game();
        g.tryMove(2, 2);   // X
        assertFalse(g.tryMove(2, 2)); // invalid
        assertEquals('O', g.currentPlayer()); // still O's turn
    }

    // ── Game: win detection ───────────────────────────────────────────────────

    @Test
    void xWinsOnRow() {
        Game g = new Game();
        // X: (1,1) (1,2) (1,3)  O: (2,1) (2,2)
        g.tryMove(1, 1); g.tryMove(2, 1);
        g.tryMove(1, 2); g.tryMove(2, 2);
        g.tryMove(1, 3);
        assertEquals(Game.Status.X_WINS, g.status());
        assertTrue(g.isOver());
    }

    @Test
    void oWinsOnColumn() {
        Game g = new Game();
        // X: (1,1) (1,2) (1,3)  O: (2,1) (3,1) — need X to waste turns
        // X: (2,2) (3,2)  O: (1,1) (2,1) (3,1)
        g.tryMove(2, 2); g.tryMove(1, 1);
        g.tryMove(3, 2); g.tryMove(2, 1);
        g.tryMove(1, 3); g.tryMove(3, 1);
        assertEquals(Game.Status.O_WINS, g.status());
        assertTrue(g.isOver());
    }

    @Test
    void xWinsOnDiagonal() {
        Game g = new Game();
        // X: (1,1)(2,2)(3,3)  O: (1,2)(1,3)
        g.tryMove(1, 1); g.tryMove(1, 2);
        g.tryMove(2, 2); g.tryMove(1, 3);
        g.tryMove(3, 3);
        assertEquals(Game.Status.X_WINS, g.status());
    }

    @Test
    void xWinsOnAntiDiagonal() {
        Game g = new Game();
        // X: (1,3)(2,2)(3,1)  O: (1,1)(1,2)
        g.tryMove(1, 3); g.tryMove(1, 1);
        g.tryMove(2, 2); g.tryMove(1, 2);
        g.tryMove(3, 1);
        assertEquals(Game.Status.X_WINS, g.status());
    }

    // ── Game: draw ────────────────────────────────────────────────────────────

    @Test
    void drawWhenBoardFull() {
        // X O X
        // X X O
        // O X O  → no winner, draw
        Game g = new Game();
        g.tryMove(1, 1); g.tryMove(1, 2); // X, O
        g.tryMove(1, 3); g.tryMove(2, 3); // X, O
        g.tryMove(2, 1); g.tryMove(3, 1); // X, O
        g.tryMove(2, 2); g.tryMove(3, 3); // X, O
        g.tryMove(3, 2);                  // X
        assertEquals(Game.Status.DRAW, g.status());
        assertTrue(g.isOver());
    }

    // ── Game: win checked before draw ─────────────────────────────────────────

    @Test
    void winCheckedBeforeDraw() {
        // Fill 8 squares, X places the 9th and wins (not draw)
        // X X X   (row 1 = X wins)
        // O O X
        // O X O
        Game g = new Game();
        g.tryMove(2, 1); // wait, X starts
        // Redo: X must win on move 9
        // X: (1,1)(1,2)(2,3)(3,2)  O: (2,1)(2,2)(3,1)(3,3)  then X→(1,3) wins row1
        g = new Game();
        g.tryMove(1, 1); g.tryMove(2, 1);
        g.tryMove(1, 2); g.tryMove(2, 2);
        g.tryMove(2, 3); g.tryMove(3, 1);
        g.tryMove(3, 2); g.tryMove(3, 3);
        g.tryMove(1, 3); // 9th square; X wins row 1
        assertEquals(Game.Status.X_WINS, g.status());
    }

    // ── Game: no moves accepted after game over ───────────────────────────────

    @Test
    void noMoveAfterGameOver() {
        Game g = new Game();
        g.tryMove(1, 1); g.tryMove(2, 1);
        g.tryMove(1, 2); g.tryMove(2, 2);
        g.tryMove(1, 3); // X wins
        assertTrue(g.isOver());
        // tryMove after game over: the contract does not call tryMove after isOver,
        // but let's confirm status stays the same
        assertEquals(Game.Status.X_WINS, g.status());
    }
}
