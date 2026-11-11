package tictactoe.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the three-pieces variant of TicTacToe (CR-2).
 * Each Player has pieceLimit=3; rounds 1–6 are Puts, round 7+ are Moves.
 * Invariants: I3, I4, I5, I6, I8, I11, I12.
 */
class ThreePiecesTest {

    private Player playerX;
    private Player playerO;
    private TicTacToe game;

    @BeforeEach
    void setUp() {
        playerX = new HumanPlayer(Color.X, g -> null);
        playerO = new HumanPlayer(Color.O, g -> null);
        game = new TicTacToe(playerX, playerO, 3);
    }

    // ── Initial state ────────────────────────────────────────────────────────

    @Test
    void I4_initialStateIsXToPut() {
        assertEquals(State.X_TO_PUT, game.state());
    }

    @Test
    void D25_piecesLeftStartsAtThreeForBoth() {
        assertEquals(3, game.piecesLeft(Color.X));
        assertEquals(3, game.piecesLeft(Color.O));
    }

    // ── Puts decrement pieces left ───────────────────────────────────────────

    @Test
    void D25_piecesLeftDecrementsAfterPut() {
        game.put(new Coordinate(1, 1)); // X puts
        assertEquals(2, game.piecesLeft(Color.X));
        assertEquals(3, game.piecesLeft(Color.O));

        game.put(new Coordinate(2, 1)); // O puts
        assertEquals(2, game.piecesLeft(Color.X));
        assertEquals(2, game.piecesLeft(Color.O));
    }

    // ── Transition to move phase ─────────────────────────────────────────────

    /**
     * After rounds 1–6 (3 Puts each), both players have 0 pieces left.
     * The state must transition to X_TO_MOVE after O's last put.
     */
    @Test
    void I12_afterSixPutsStateIsXToMove() {
        // Round 1: X at (1,1)
        game.put(new Coordinate(1, 1));
        // Round 2: O at (2,1)
        game.put(new Coordinate(2, 1));
        // Round 3: X at (1,2)
        game.put(new Coordinate(1, 2));
        // Round 4: O at (2,2)
        game.put(new Coordinate(2, 2));
        // Round 5: X at (3,1) — X now has 0 pieces left but O still has 1
        game.put(new Coordinate(3, 1));
        assertEquals(0, game.piecesLeft(Color.X));
        assertEquals(1, game.piecesLeft(Color.O));
        assertEquals(State.O_TO_PUT, game.state()); // O still has 1 piece to put
        // Round 6: O at (3,2) — O now has 0 pieces left; both are 0 → move phase
        game.put(new Coordinate(3, 2));
        assertEquals(0, game.piecesLeft(Color.X));
        assertEquals(0, game.piecesLeft(Color.O));
        assertEquals(State.X_TO_MOVE, game.state());
    }

    @Test
    void I12_putRejectedInMovePhase() {
        doSixPuts();
        assertEquals(State.X_TO_MOVE, game.state());
        assertFalse(game.put(new Coordinate(1, 3)));
        assertEquals(State.X_TO_MOVE, game.state()); // state unchanged
    }

    // ── Valid moves ──────────────────────────────────────────────────────────

    @Test
    void I11_validMoveReturnsTrue() {
        doSixPuts();
        // X is at (1,1) and (1,2) and (3,1); move (1,1) → (1,3)
        assertTrue(game.move(new Coordinate(1, 1), new Coordinate(1, 3)));
    }

    @Test
    void I11_validMoveUpdatesBoardAndState() {
        doSixPuts();
        // Before: X at (1,1),(1,2),(3,1); O at (2,1),(2,2),(3,2)
        game.move(new Coordinate(1, 1), new Coordinate(1, 3));
        assertEquals(Color.EMPTY, game.colorAt(new Coordinate(1, 1)));
        assertEquals(Color.X, game.colorAt(new Coordinate(1, 3)));
        assertEquals(State.O_TO_MOVE, game.state());
    }

    @Test
    void I4_moveAlternatesPlayers() {
        doSixPuts();
        assertEquals(playerX, game.currentPlayer());
        game.move(new Coordinate(1, 1), new Coordinate(1, 3));
        assertEquals(playerO, game.currentPlayer());
    }

    // ── Invalid move rejections ───────────────────────────────────────────────

    @Test
    void I11_moveFromEmptySquareIsRejected() {
        doSixPuts();
        // (1,3) is empty — X cannot move from there
        assertFalse(game.move(new Coordinate(1, 3), new Coordinate(3, 3)));
        assertEquals(State.X_TO_MOVE, game.state()); // state unchanged
    }

    @Test
    void I11_moveFromOpponentPieceIsRejected() {
        doSixPuts();
        // (2,1) has O's piece — X cannot move from there
        assertFalse(game.move(new Coordinate(2, 1), new Coordinate(1, 3)));
        assertEquals(State.X_TO_MOVE, game.state()); // state unchanged
    }

    @Test
    void I11_moveToOccupiedSquareIsRejected() {
        doSixPuts();
        // (2,1) has O's piece — X cannot move to an occupied square
        assertFalse(game.move(new Coordinate(1, 1), new Coordinate(2, 1)));
        assertEquals(State.X_TO_MOVE, game.state()); // state unchanged
    }

    @Test
    void I11_moveDoesNotChangeBoardWhenRejected() {
        doSixPuts();
        Color beforeFrom = game.colorAt(new Coordinate(1, 1));
        Color beforeTo   = game.colorAt(new Coordinate(2, 1));
        game.move(new Coordinate(1, 1), new Coordinate(2, 1)); // rejected
        assertEquals(beforeFrom, game.colorAt(new Coordinate(1, 1)));
        assertEquals(beforeTo, game.colorAt(new Coordinate(2, 1)));
    }

    // ── move() is rejected outside the move phase ─────────────────────────────

    @Test
    void I8_moveRejectedInPutPhase() {
        // Before any puts, the game is in put phase
        assertFalse(game.move(new Coordinate(1, 1), new Coordinate(1, 2)));
    }

    // ── Win detection in move phase ───────────────────────────────────────────

    @Test
    void I6_xWinsInMovePhase() {
        // Board after 6 puts (no win yet):
        // X at (1,1),(1,2),(3,1); O at (2,1),(2,2),(3,2)
        // X moves (3,1) → (1,3) to complete row 1: (1,1)(1,2)(1,3)
        doSixPuts();
        assertTrue(game.move(new Coordinate(3, 1), new Coordinate(1, 3)));
        assertEquals(State.X_WINS, game.state());
    }

    @Test
    void I6_oWinsInMovePhase() {
        // X at (1,1),(1,2),(3,1); O at (2,1),(2,2),(3,2)
        // X makes a neutral move, then O completes column 2: (1,2)(2,2)(3,2)
        // But (1,2) is X's piece, not O's. Let's adjust:
        // X moves (1,1) → (1,3) (neutral), then O moves (2,1) → (1,1) (O completes column 1? no)
        // O at (2,1),(2,2),(3,2). O moves (2,1) → (1,1) → O has (1,1),(2,2),(3,2): no win.
        // Let O try column 2: O at (2,2),(3,2), needs (1,2) but that's X's.
        // For row 2 O needs (2,1),(2,2),(2,3): after neutral X move, O moves (3,2) → (2,3)
        //   O has (2,1),(2,2),(2,3) → wins row 2!
        doSixPuts();
        game.move(new Coordinate(1, 1), new Coordinate(1, 3)); // X neutral move
        assertTrue(game.move(new Coordinate(3, 2), new Coordinate(2, 3))); // O completes row 2
        assertEquals(State.O_WINS, game.state());
    }

    @Test
    void I12_noDrawInThreePiecesVariant() {
        // Even after 6 puts, the game should still be in progress (no draw possible)
        doSixPuts();
        assertFalse(game.state().isOver());
        assertFalse(game.state() == State.DRAW);
    }

    // ── Win can also happen during the put phase ──────────────────────────────

    @Test
    void I6_xWinsInPutPhase_stillWorks() {
        // X: (1,1) (1,2) (1,3) — wins in round 5 (before 6 puts, in put phase)
        game.put(new Coordinate(1, 1)); // X round 1
        game.put(new Coordinate(2, 1)); // O round 2
        game.put(new Coordinate(1, 2)); // X round 3
        game.put(new Coordinate(2, 2)); // O round 4
        game.put(new Coordinate(1, 3)); // X round 5 — wins row 1
        assertEquals(State.X_WINS, game.state());
    }

    // ── piecesLeft is MAX_VALUE for standard variant ───────────────────────────

    @Test
    void D25_piecesLeftIsMaxValueForStandardVariant() {
        TicTacToe standardGame = new TicTacToe(playerX, playerO);
        assertEquals(Integer.MAX_VALUE, standardGame.piecesLeft(Color.X));
        assertEquals(Integer.MAX_VALUE, standardGame.piecesLeft(Color.O));
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    /**
     * Makes 6 valid Puts with no win:
     * X at (1,1), (1,2), (3,1)
     * O at (2,1), (2,2), (3,2)
     */
    private void doSixPuts() {
        game.put(new Coordinate(1, 1)); // X round 1
        game.put(new Coordinate(2, 1)); // O round 2
        game.put(new Coordinate(1, 2)); // X round 3
        game.put(new Coordinate(2, 2)); // O round 4
        game.put(new Coordinate(3, 1)); // X round 5
        game.put(new Coordinate(3, 2)); // O round 6 → X_TO_MOVE
    }
}
