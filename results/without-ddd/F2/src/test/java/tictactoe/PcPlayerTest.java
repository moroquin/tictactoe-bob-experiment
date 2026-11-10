package tictactoe;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PcPlayer's deterministic move selection.
 */
class PcPlayerTest {

    private final PcPlayer pc = new PcPlayer();

    // ── Rule 1: Win ──────────────────────────────────────────────────────────

    @Test
    void winOnRow() {
        // O has (2,1) and (2,2) — completing row 2 at (2,3) wins
        Board b = new Board();
        b.place(1, 1, 'X');
        b.place(2, 1, 'O');
        b.place(1, 2, 'X');
        b.place(2, 2, 'O');
        int[] move = pc.chooseMove(b, 'O');
        assertArrayEquals(new int[]{2, 3}, move, "should complete row 2 to win");
    }

    @Test
    void winOnColumn() {
        // O has (1,3) and (2,3) — completing col 3 at (3,3) wins
        Board b = new Board();
        b.place(1, 1, 'X');
        b.place(1, 3, 'O');
        b.place(2, 2, 'X');
        b.place(2, 3, 'O');
        int[] move = pc.chooseMove(b, 'O');
        assertArrayEquals(new int[]{3, 3}, move, "should complete col 3 to win");
    }

    @Test
    void winOnDiagonal() {
        // O has (1,1) and (2,2) — completing main diagonal at (3,3) wins
        Board b = new Board();
        b.place(1, 2, 'X');
        b.place(1, 1, 'O');
        b.place(1, 3, 'X');
        b.place(2, 2, 'O');
        int[] move = pc.chooseMove(b, 'O');
        assertArrayEquals(new int[]{3, 3}, move, "should complete main diagonal to win");
    }

    // ── Rule 2: Block ────────────────────────────────────────────────────────

    @Test
    void blockOpponentRow() {
        // X has (1,1) and (1,2) — PC must block at (1,3)
        Board b = new Board();
        b.place(1, 1, 'X');
        b.place(1, 2, 'X');
        int[] move = pc.chooseMove(b, 'O');
        assertArrayEquals(new int[]{1, 3}, move, "should block X from completing row 1");
    }

    @Test
    void blockOpponentColumn() {
        // X has (1,1) and (2,1) — PC must block at (3,1)
        Board b = new Board();
        b.place(1, 1, 'X');
        b.place(2, 1, 'X');
        int[] move = pc.chooseMove(b, 'O');
        assertArrayEquals(new int[]{3, 1}, move, "should block X from completing col 1");
    }

    @Test
    void blockOpponentDiagonal() {
        // X has (1,3) and (2,2) — block anti-diagonal at (3,1)
        Board b = new Board();
        b.place(1, 3, 'X');
        b.place(2, 2, 'X');
        int[] move = pc.chooseMove(b, 'O');
        assertArrayEquals(new int[]{3, 1}, move, "should block X's anti-diagonal");
    }

    // ── Rule 3: Center ───────────────────────────────────────────────────────

    @Test
    void takeCenterWhenEmpty() {
        Board b = new Board();
        b.place(1, 1, 'X');
        int[] move = pc.chooseMove(b, 'O');
        assertArrayEquals(new int[]{2, 2}, move, "should take center (2,2)");
    }

    // ── Rule 4: Corner ───────────────────────────────────────────────────────

    @Test
    void takeFirstEmptyCornerWhenCenterTaken() {
        // Center (2,2) is occupied by X; (1,1) is the first corner
        Board b = new Board();
        b.place(2, 2, 'X');
        int[] move = pc.chooseMove(b, 'O');
        assertArrayEquals(new int[]{1, 1}, move, "should take first empty corner (1,1)");
    }

    @Test
    void takeNextCornerIfFirstTaken() {
        // Center taken by O, (1,1) taken by X (single piece — no 2-in-a-line threat).
        // No win/block situation → PC should take the next empty corner (1,3).
        Board b = new Board();
        b.place(2, 2, 'O'); // center occupied
        b.place(1, 1, 'X'); // first corner occupied by opponent (single piece, no threat)
        int[] move = pc.chooseMove(b, 'O');
        assertArrayEquals(new int[]{1, 3}, move, "should take second corner (1,3)");
    }

    // ── Rule 5: Fallback ─────────────────────────────────────────────────────

    @Test
    void fallbackToFirstEmptySquare() {
        // All corners and center occupied; no win/block situation
        Board b = new Board();
        b.place(1, 1, 'X');
        b.place(1, 3, 'O');
        b.place(2, 2, 'X');
        b.place(3, 1, 'O');
        b.place(3, 3, 'X');
        // Empty squares: (1,2),(2,1),(2,3),(3,2) — first is (1,2)
        // No win/block lines exist for O at this state
        int[] move = pc.chooseMove(b, 'O');
        assertArrayEquals(new int[]{1, 2}, move, "should fall back to (1,2)");
    }

    // ── Win takes priority over block ────────────────────────────────────────

    @Test
    void winBeforeBlock() {
        // O can win at (3,3) AND X threatens at (1,3) — must choose win
        Board b = new Board();
        b.place(1, 1, 'X');
        b.place(2, 1, 'O');
        b.place(1, 2, 'X');
        b.place(2, 2, 'O');
        // O can complete row 2 at (2,3); X threatens row 1 at (1,3)
        int[] move = pc.chooseMove(b, 'O');
        assertArrayEquals(new int[]{2, 3}, move, "win must take priority over block");
    }

    // ── Reading-order tie-break for rule 1/2 ────────────────────────────────

    @Test
    void winReadingOrderTieBreak() {
        // O has two winning options; must pick the first in reading order
        // O at (1,1),(1,2) → win at (1,3); also O at (1,1),(2,1) → win at (3,1)
        // (1,3) comes before (3,1) in reading order
        Board b = new Board();
        b.place(1, 1, 'O');
        b.place(1, 2, 'O');
        b.place(2, 1, 'O');
        // Rows (line index 0): (1,1)(1,2)(1,3) → O,O,. → win at (1,3)
        // Col (line index 3): (1,1)(2,1)(3,1) → O,O,. → win at (3,1)
        // First line checked is row 1 → (1,3)
        int[] move = pc.chooseMove(b, 'O');
        assertArrayEquals(new int[]{1, 3}, move, "tie-break: first win in reading order");
    }
}
