package tictactoe.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThreePiecesTicTacToeTest {

    private Player playerX;
    private Player playerO;
    private ThreePiecesTicTacToe game;

    @BeforeEach
    void setUp() {
        playerX = new HumanPlayer(Color.X);
        playerO = new HumanPlayer(Color.O);
        game = new ThreePiecesTicTacToe(playerX, playerO);
    }

    // ---- Initial state ----

    @Test
    void I4_initialStateIsXToPut() {
        assertEquals(State.X_TO_PUT, game.state());
    }

    @Test
    void I4_initialCurrentPlayerIsX() {
        assertEquals(playerX, game.currentPlayer());
    }

    @Test
    void I1_allSquaresStartEmpty() {
        for (Coordinate c : Coordinate.all()) {
            assertEquals(Color.EMPTY, game.colorAt(c));
        }
    }

    // ---- Put phase: 6 rounds of alternating puts ----

    @Test
    void I11_afterXPutsStateIsOToPut() {
        assertTrue(game.put(new Coordinate(1, 1)));
        assertEquals(State.O_TO_PUT, game.state());
        assertEquals(playerO, game.currentPlayer());
    }

    @Test
    void I11_afterThreePutsEachStateTransitionsToMovePhase() {
        // X puts 3, O puts 3 → both have 0 remaining → X_TO_MOVE
        game.put(new Coordinate(1, 1)); // X round 1
        game.put(new Coordinate(2, 1)); // O round 2
        game.put(new Coordinate(1, 2)); // X round 3
        game.put(new Coordinate(2, 2)); // O round 4
        game.put(new Coordinate(3, 3)); // X round 5 — X has 0 left, O still has 1
        assertEquals(State.O_TO_PUT, game.state());
        game.put(new Coordinate(3, 1)); // O round 6 — both have 0 left, next is X
        assertEquals(State.X_TO_MOVE, game.state());
        assertEquals(playerX, game.currentPlayer());
    }

    @Test
    void I12_putIsRejectedInMoveState() {
        putAllSixPieces();
        // now in X_TO_MOVE
        assertFalse(game.put(new Coordinate(1, 3)));
        assertEquals(State.X_TO_MOVE, game.state());
    }

    // ---- Occupied square rejection during put phase ----

    @Test
    void I3_putOnOccupiedSquareIsRejected() {
        game.put(new Coordinate(1, 1));  // X
        assertFalse(game.put(new Coordinate(1, 1)));  // O tries same square
        assertEquals(State.O_TO_PUT, game.state());
    }

    // ---- Win during put phase ----

    @Test
    void I14_xWinsOnRowDuringPutPhase() {
        // X: row 1; O: row 2 — X wins on round 5 (3rd put)
        game.put(new Coordinate(1, 1)); // X round 1
        game.put(new Coordinate(2, 1)); // O round 2
        game.put(new Coordinate(1, 2)); // X round 3
        game.put(new Coordinate(2, 2)); // O round 4
        game.put(new Coordinate(1, 3)); // X round 5 — wins!
        assertEquals(State.X_WINS, game.state());
        assertTrue(game.state().isOver());
    }

    @Test
    void I14_oWinsOnColumnDuringPutPhase() {
        game.put(new Coordinate(1, 1)); // X
        game.put(new Coordinate(1, 2)); // O
        game.put(new Coordinate(2, 1)); // X
        game.put(new Coordinate(2, 2)); // O
        game.put(new Coordinate(3, 3)); // X
        game.put(new Coordinate(3, 2)); // O wins column 2
        assertEquals(State.O_WINS, game.state());
    }

    // ---- Move phase ----

    @Test
    void I13_moveInvalidWhenNotInMoveState() {
        // game is in X_TO_PUT
        assertFalse(game.move(new Coordinate(1, 1), new Coordinate(1, 2)));
    }

    @Test
    void I13_moveRejectedWhenOriginDoesNotHoldCurrentPlayerPiece() {
        putAllSixPieces(); // board: X@(1,1),(1,2),(3,3), O@(2,1),(2,2),(3,1), state=X_TO_MOVE
        // X tries to move from O's square
        assertFalse(game.move(new Coordinate(2, 1), new Coordinate(1, 3)));
        assertEquals(State.X_TO_MOVE, game.state());
    }

    @Test
    void I13_moveRejectedWhenOriginIsEmpty() {
        putAllSixPieces();
        // X tries to move from empty square
        assertFalse(game.move(new Coordinate(1, 3), new Coordinate(2, 3)));
        assertEquals(State.X_TO_MOVE, game.state());
    }

    @Test
    void I13_moveRejectedWhenTargetIsOccupied() {
        putAllSixPieces(); // X@(1,1),(1,2),(3,3), O@(2,1),(2,2),(3,1)
        // X tries to move to an occupied square
        assertFalse(game.move(new Coordinate(1, 1), new Coordinate(2, 1)));
        assertEquals(State.X_TO_MOVE, game.state());
    }

    @Test
    void I13_validMoveSucceedsAndAlternates() {
        putAllSixPieces(); // state=X_TO_MOVE
        // X moves (1,1) to (1,3)
        assertTrue(game.move(new Coordinate(1, 1), new Coordinate(1, 3)));
        assertEquals(Color.EMPTY, game.colorAt(new Coordinate(1, 1)));
        assertEquals(Color.X,     game.colorAt(new Coordinate(1, 3)));
        assertEquals(State.O_TO_MOVE, game.state());
        assertEquals(playerO, game.currentPlayer());
    }

    @Test
    void I14_xWinsOnRowDuringMovePhase() {
        putAllSixPieces(); // X@(1,1),(1,2),(3,3), O@(2,1),(2,2),(3,1), state=X_TO_MOVE
        // X moves (3,3) to (1,3) to complete row 1
        assertTrue(game.move(new Coordinate(3, 3), new Coordinate(1, 3)));
        assertEquals(State.X_WINS, game.state());
    }

    @Test
    void I14_oWinsOnRowDuringMovePhase() {
        // Set up: O gets row 2 after some moves
        game.put(new Coordinate(1, 1)); // X round 1
        game.put(new Coordinate(2, 1)); // O round 2
        game.put(new Coordinate(1, 2)); // X round 3
        game.put(new Coordinate(2, 2)); // O round 4
        game.put(new Coordinate(3, 3)); // X round 5
        game.put(new Coordinate(3, 1)); // O round 6 — both done, X_TO_MOVE
        // board: X@(1,1),(1,2),(3,3), O@(2,1),(2,2),(3,1), state=X_TO_MOVE
        // X moves (3,3) to (3,2) (not a win)
        game.move(new Coordinate(3, 3), new Coordinate(3, 2)); // → O_TO_MOVE
        // O moves (3,1) to (2,3) to complete row 2
        assertTrue(game.move(new Coordinate(3, 1), new Coordinate(2, 3)));
        assertEquals(State.O_WINS, game.state());
    }

    @Test
    void I15_noDrawInThreePiecesVariant() {
        // After all pieces are placed, the board has 6 of 9 squares occupied,
        // so DRAW is never reached. Verify game continues in move phase.
        putAllSixPieces();
        assertFalse(game.state().isOver());
        assertFalse(game.state() == State.DRAW);
    }

    @Test
    void I8_putAfterGameOverIsRejected() {
        // Win on round 5
        game.put(new Coordinate(1, 1)); // X
        game.put(new Coordinate(2, 1)); // O
        game.put(new Coordinate(1, 2)); // X
        game.put(new Coordinate(2, 2)); // O
        game.put(new Coordinate(1, 3)); // X wins
        assertFalse(game.put(new Coordinate(3, 1)));
        assertEquals(State.X_WINS, game.state());
    }

    @Test
    void I8_moveAfterGameOverIsRejected() {
        putAllSixPieces(); // X_TO_MOVE
        game.move(new Coordinate(3, 3), new Coordinate(1, 3)); // X wins (row 1)
        assertEquals(State.X_WINS, game.state());
        assertFalse(game.move(new Coordinate(2, 1), new Coordinate(1, 1)));
    }

    // ---- Board state preservation on rejection ----

    @Test
    void I13_boardDoesNotChangeAfterRejectedMove() {
        putAllSixPieces();
        // X tries to move from O's square — rejected
        game.move(new Coordinate(2, 1), new Coordinate(1, 3));
        assertEquals(Color.O, game.colorAt(new Coordinate(2, 1)));
        assertEquals(Color.EMPTY, game.colorAt(new Coordinate(1, 3)));
    }

    // ---- Helpers ----

    /**
     * Puts all 6 initial pieces without a win.
     * Result: X@(1,1),(1,2),(3,3), O@(2,1),(2,2),(3,1), state=X_TO_MOVE.
     */
    private void putAllSixPieces() {
        game.put(new Coordinate(1, 1)); // X round 1
        game.put(new Coordinate(2, 1)); // O round 2
        game.put(new Coordinate(1, 2)); // X round 3
        game.put(new Coordinate(2, 2)); // O round 4
        game.put(new Coordinate(3, 3)); // X round 5
        game.put(new Coordinate(3, 1)); // O round 6 → X_TO_MOVE
        assertEquals(State.X_TO_MOVE, game.state());
    }
}
