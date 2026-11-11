package tictactoe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the deterministic PC player strategy.
 */
class PcPlayerTest {

    // -------------------------------------------------------------------------
    // Rule 1: Win — PC takes the winning square
    // -------------------------------------------------------------------------

    @Test
    void rule1_pcWinsOnRow() {
        // O at (2,1),(2,2); X at (1,1),(1,2),(3,1) — O can win at (2,3).
        // Move sequence: X(1,1),O(2,1),X(1,2),O(2,2),X(3,1) → O's turn
        Board board = new Board()
            .place(1, 1).place(2, 1)
            .place(1, 2).place(2, 2)
            .place(3, 1);
        assertEquals(Color.O, board.currentPlayer());
        int[] move = PcPlayer.chooseMove(board);
        assertArrayEquals(new int[]{2, 3}, move);
    }

    @Test
    void rule1_pcWinsOnColumn() {
        // O at (1,3),(2,3); X at (1,1),(2,2),(3,2) — O can win at (3,3).
        // Move sequence: X(1,1),O(1,3),X(2,2),O(2,3),X(3,2) → O's turn
        Board board = new Board()
            .place(1, 1).place(1, 3)
            .place(2, 2).place(2, 3)
            .place(3, 2);
        assertEquals(Color.O, board.currentPlayer());
        int[] move = PcPlayer.chooseMove(board);
        assertArrayEquals(new int[]{3, 3}, move);
    }

    @Test
    void rule1_pcWinsOnDiagonal() {
        // O at (1,1),(2,2); X at (1,2),(1,3),(2,3) — O can win at (3,3).
        // Move sequence: X(1,2),O(1,1),X(1,3),O(2,2),X(2,3) → O's turn
        Board board = new Board()
            .place(1, 2).place(1, 1)
            .place(1, 3).place(2, 2)
            .place(2, 3);
        assertEquals(Color.O, board.currentPlayer());
        int[] move = PcPlayer.chooseMove(board);
        assertArrayEquals(new int[]{3, 3}, move);
    }

    // -------------------------------------------------------------------------
    // Rule 1 beats Rule 2: PC wins rather than blocks
    // -------------------------------------------------------------------------

    @Test
    void rule1_preferWinOverBlock() {
        // O can win at (2,3); X threatens row 1 at (1,3) — rule 1 takes priority.
        // Move sequence: X(1,1),O(2,1),X(1,2),O(2,2),X(3,1) → O's turn
        Board board = new Board()
            .place(1, 1).place(2, 1)
            .place(1, 2).place(2, 2)
            .place(3, 1);
        assertEquals(Color.O, board.currentPlayer());
        int[] move = PcPlayer.chooseMove(board);
        assertArrayEquals(new int[]{2, 3}, move);
    }

    // -------------------------------------------------------------------------
    // Rule 2: Block — PC blocks X from winning
    // -------------------------------------------------------------------------

    @Test
    void rule2_blockXWinOnRow() {
        // X at (1,1),(1,2) threatens (1,3); O at (3,1) — no O win, must block.
        // Move sequence: X(1,1),O(3,1),X(1,2) → O's turn
        Board board = new Board()
            .place(1, 1).place(3, 1)
            .place(1, 2);
        assertEquals(Color.O, board.currentPlayer());
        int[] move = PcPlayer.chooseMove(board);
        assertArrayEquals(new int[]{1, 3}, move);
    }

    @Test
    void rule2_blockXWinOnColumn() {
        // X at (1,1),(2,1) threatens (3,1); O at (1,3) — no O win, must block.
        // Move sequence: X(1,1),O(1,3),X(2,1) → O's turn
        Board board = new Board()
            .place(1, 1).place(1, 3)
            .place(2, 1);
        assertEquals(Color.O, board.currentPlayer());
        int[] move = PcPlayer.chooseMove(board);
        assertArrayEquals(new int[]{3, 1}, move);
    }

    // -------------------------------------------------------------------------
    // Rule 3: Center
    // -------------------------------------------------------------------------

    @Test
    void rule3_centerWhenNoWinOrBlock() {
        // X at (1,1); no threats, center empty — O should go to (2,2).
        Board board = new Board().place(1, 1);
        assertEquals(Color.O, board.currentPlayer());
        int[] move = PcPlayer.chooseMove(board);
        assertArrayEquals(new int[]{2, 2}, move);
    }

    // -------------------------------------------------------------------------
    // Rule 4: Corner
    // -------------------------------------------------------------------------

    @Test
    void rule4_firstCornerWhenCenterTaken() {
        // X at (2,1),(3,3); O at (2,2) [center] — no threats, (1,1) is first empty corner.
        // Move sequence: X(2,1),O(2,2),X(3,3) → O's turn
        Board board = new Board()
            .place(2, 1).place(2, 2)
            .place(3, 3);
        assertEquals(Color.O, board.currentPlayer());
        int[] move = PcPlayer.chooseMove(board);
        assertArrayEquals(new int[]{1, 1}, move);
    }

    @Test
    void rule4_cornerPickedWhenFirstCornerOccupied() {
        // Center taken; (1,1) occupied by X; (1,3) is next empty corner.
        // X: (1,1),(3,2); O: (2,2) — no threats.
        // Move sequence: X(1,1),O(2,2),X(3,2) → O's turn
        Board board = new Board()
            .place(1, 1).place(2, 2)
            .place(3, 2);
        assertEquals(Color.O, board.currentPlayer());
        int[] move = PcPlayer.chooseMove(board);
        assertArrayEquals(new int[]{1, 3}, move);
    }

    // -------------------------------------------------------------------------
    // Rule 5: First empty in reading order
    // -------------------------------------------------------------------------

    @Test
    void rule5_firstEmptySquare() {
        // Board (O's turn): X O X / O X X / . . O — only (3,1) and (3,2) are empty.
        // No O win and no X threat (all X lines are blocked by O).
        // Move sequence: X(1,1),O(1,2),X(1,3),O(2,1),X(2,2),O(3,3),X(2,3) → O's turn
        Board board = new Board()
            .place(1, 1).place(1, 2)
            .place(1, 3).place(2, 1)
            .place(2, 2).place(3, 3)
            .place(2, 3);
        assertEquals(Color.O, board.currentPlayer());
        int[] move = PcPlayer.chooseMove(board);
        assertArrayEquals(new int[]{3, 1}, move);
    }

    // -------------------------------------------------------------------------
    // Rule 1: first winning square in reading order wins when there are several
    // -------------------------------------------------------------------------

    @Test
    void rule1_multipleWinsPicksFirstInReadingOrder() {
        // O at (2,1),(2,2),(1,2): wins at (2,3) [row 2] and (3,2) [col 2].
        // X at (1,1),(1,3),(3,1),(3,3) — no X threats (all lines blocked by O).
        // First winning square in reading order: (2,3).
        // Move sequence: X(1,1),O(2,1),X(1,3),O(2,2),X(3,1),O(1,2),X(3,3) → O's turn
        Board board = new Board()
            .place(1, 1).place(2, 1)
            .place(1, 3).place(2, 2)
            .place(3, 1).place(1, 2)
            .place(3, 3);
        assertEquals(Color.O, board.currentPlayer());
        int[] move = PcPlayer.chooseMove(board);
        assertArrayEquals(new int[]{2, 3}, move);
    }
}
