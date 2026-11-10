package tictactoe;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the three-pieces variant rules (ThreePiecesGame).
 */
class ThreePiecesGameTest {

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    void xStartsFirst() {
        assertEquals('X', new ThreePiecesGame().currentPlayer());
    }

    @Test
    void startsInPutPhase() {
        assertTrue(new ThreePiecesGame().isPutPhase());
    }

    @Test
    void startsOngoing() {
        assertEquals(ThreePiecesGame.Status.ONGOING, new ThreePiecesGame().status());
    }

    // ── Put phase transitions ─────────────────────────────────────────────────

    @Test
    void turnsAlternateDuringPuts() {
        ThreePiecesGame g = new ThreePiecesGame();
        assertTrue(g.tryPut(1, 1));   // X
        assertEquals('O', g.currentPlayer());
        assertTrue(g.tryPut(2, 1));   // O
        assertEquals('X', g.currentPlayer());
    }

    @Test
    void sixPutsExhaustsAllPieces() {
        ThreePiecesGame g = new ThreePiecesGame();
        // Place all 6 pieces without winning
        g.tryPut(1, 1); // X
        g.tryPut(2, 1); // O
        g.tryPut(1, 2); // X
        g.tryPut(2, 2); // O
        g.tryPut(3, 1); // X  — X has 3, O has 2 at this point
        g.tryPut(3, 2); // O  — O has 3
        // Neither won on those squares; now move phase
        assertFalse(g.isPutPhase(), "should be in move phase after 6 puts");
    }

    @Test
    void rejectPutWhenSquareOccupied() {
        ThreePiecesGame g = new ThreePiecesGame();
        g.tryPut(1, 1);
        assertFalse(g.tryPut(1, 1)); // O cannot place on X's square
        assertEquals('O', g.currentPlayer()); // still O's turn
    }

    @Test
    void rejectPutOutOfRange() {
        ThreePiecesGame g = new ThreePiecesGame();
        assertFalse(g.tryPut(0, 1));
        assertFalse(g.tryPut(4, 1));
        assertFalse(g.tryPut(1, 0));
        assertFalse(g.tryPut(1, 4));
        assertEquals('X', g.currentPlayer()); // still X's turn
    }

    // ── Win during put phase ──────────────────────────────────────────────────

    @Test
    void xWinsDuringPutPhase() {
        ThreePiecesGame g = new ThreePiecesGame();
        // X: (1,1) (1,2) (1,3)  O: (2,1) (2,2)
        g.tryPut(1, 1); g.tryPut(2, 1);
        g.tryPut(1, 2); g.tryPut(2, 2);
        g.tryPut(1, 3); // X completes row 1
        assertEquals(ThreePiecesGame.Status.X_WINS, g.status());
        assertTrue(g.isOver());
    }

    @Test
    void oWinsDuringPutPhase() {
        ThreePiecesGame g = new ThreePiecesGame();
        // O wins col 1: O at (1,1)(2,1)(3,1); X wastes turns
        g.tryPut(1, 2); g.tryPut(1, 1);  // X, O
        g.tryPut(2, 2); g.tryPut(2, 1);  // X, O
        g.tryPut(3, 3); g.tryPut(3, 1);  // X, O — O completes col 1
        assertEquals(ThreePiecesGame.Status.O_WINS, g.status());
        assertTrue(g.isOver());
    }

    // ── Move phase ────────────────────────────────────────────────────────────

    /**
     * Helper: reaches move phase with a board that has no winner yet.
     * X: (1,1)(1,2)(3,1)  O: (2,1)(2,2)(3,2)
     * Neither row/col/diagonal complete.
     */
    private ThreePiecesGame reachMovePhase() {
        ThreePiecesGame g = new ThreePiecesGame();
        g.tryPut(1, 1); g.tryPut(2, 1);
        g.tryPut(1, 2); g.tryPut(2, 2);
        g.tryPut(3, 1); g.tryPut(3, 2);
        return g;
    }

    @Test
    void movePhaseBeginsAfterSixPuts() {
        ThreePiecesGame g = reachMovePhase();
        assertFalse(g.isPutPhase());
        assertEquals('X', g.currentPlayer());
    }

    @Test
    void validMoveMovesPiece() {
        ThreePiecesGame g = reachMovePhase();
        // X is at (1,1); move it to (1,3)
        assertTrue(g.tryMove(1, 1, 1, 3));
        assertEquals('.', g.board().get(1, 1));
        assertEquals('X', g.board().get(1, 3));
        assertEquals('O', g.currentPlayer()); // turn changes
    }

    @Test
    void rejectMoveWrongPiece() {
        ThreePiecesGame g = reachMovePhase();
        // (2,1) belongs to O; X tries to move it
        assertFalse(g.tryMove(2, 1, 2, 3));
        assertEquals('X', g.currentPlayer()); // still X's turn
    }

    @Test
    void rejectMoveToOccupiedSquare() {
        ThreePiecesGame g = reachMovePhase();
        // X at (1,1) tries to move to (1,2) which is also X — occupied
        assertFalse(g.tryMove(1, 1, 1, 2));
        assertEquals('X', g.currentPlayer());
    }

    @Test
    void rejectMoveOutOfRange() {
        ThreePiecesGame g = reachMovePhase();
        assertFalse(g.tryMove(0, 1, 1, 3));
        assertFalse(g.tryMove(1, 1, 4, 3));
        assertEquals('X', g.currentPlayer());
    }

    @Test
    void rejectMoveFromEmptySquare() {
        ThreePiecesGame g = reachMovePhase();
        // (1,3) is empty — cannot move from there
        assertFalse(g.tryMove(1, 3, 2, 3));
        assertEquals('X', g.currentPlayer());
    }

    // ── Win during move phase ─────────────────────────────────────────────────

    @Test
    void xWinsDuringMovePhase() {
        ThreePiecesGame g = reachMovePhase();
        // Board: X at (1,1)(1,2)(3,1); O at (2,1)(2,2)(3,2)
        // X moves (3,1) → (1,3): X has (1,1)(1,2)(1,3) = row 1 win
        assertTrue(g.tryMove(3, 1, 1, 3));
        assertEquals(ThreePiecesGame.Status.X_WINS, g.status());
        assertTrue(g.isOver());
    }

    @Test
    void oWinsDuringMovePhase() {
        ThreePiecesGame g = reachMovePhase();
        // X moves a piece that doesn't win first
        g.tryMove(3, 1, 3, 3); // X: now at (1,1)(1,2)(3,3)
        // O at (2,1)(2,2)(3,2); move (3,2) → (2,3): O has (2,1)(2,2)(2,3) = row 2 win
        assertTrue(g.tryMove(3, 2, 2, 3));
        assertEquals(ThreePiecesGame.Status.O_WINS, g.status());
        assertTrue(g.isOver());
    }

    // ── No draw in this variant ───────────────────────────────────────────────

    @Test
    void noDrawStatusExists() {
        // The Status enum should only have ONGOING, X_WINS, O_WINS
        ThreePiecesGame.Status[] values = ThreePiecesGame.Status.values();
        assertEquals(3, values.length);
        assertEquals(ThreePiecesGame.Status.ONGOING, values[0]);
        assertEquals(ThreePiecesGame.Status.X_WINS,  values[1]);
        assertEquals(ThreePiecesGame.Status.O_WINS,  values[2]);
    }

    // ── put is rejected when move phase is active ─────────────────────────────

    @Test
    void tryPutRejectedInMovePhase() {
        ThreePiecesGame g = reachMovePhase();
        assertFalse(g.tryPut(1, 3)); // can't add a 4th piece
        assertEquals('X', g.currentPlayer());
    }

    // ── tryMove rejected in put phase ─────────────────────────────────────────

    @Test
    void tryMoveRejectedInPutPhase() {
        ThreePiecesGame g = new ThreePiecesGame();
        g.tryPut(1, 1); // X places once
        // Still in put phase; tryMove should be rejected
        assertFalse(g.tryMove(1, 1, 1, 2));
        assertEquals('O', g.currentPlayer()); // O's turn for putting
    }
}
