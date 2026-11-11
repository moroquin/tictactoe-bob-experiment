package tictactoe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ThreePiecesBoard — the three-pieces variant.
 */
class ThreePiecesBoardTest {

    // -------------------------------------------------------------------------
    // Initial state
    // -------------------------------------------------------------------------

    @Test
    void newBoardIsEmpty() {
        ThreePiecesBoard board = new ThreePiecesBoard();
        for (int r = 1; r <= 3; r++) {
            for (int c = 1; c <= 3; c++) {
                assertNull(board.at(r, c));
            }
        }
    }

    @Test
    void newBoardIsInPutPhase() {
        assertTrue(new ThreePiecesBoard().isPutPhase());
    }

    @Test
    void xGoesFirst() {
        assertEquals(Color.X, new ThreePiecesBoard().currentPlayer());
    }

    // -------------------------------------------------------------------------
    // Put phase: 6 puts
    // -------------------------------------------------------------------------

    @Test
    void playersAlternateDuringPutPhase() {
        ThreePiecesBoard board = new ThreePiecesBoard().place(1, 1);
        assertEquals(Color.O, board.currentPlayer());
        board = board.place(2, 1);
        assertEquals(Color.X, board.currentPlayer());
    }

    @Test
    void afterSixPutsEntersMovePhase() {
        ThreePiecesBoard board = sixPuts();
        assertFalse(board.isPutPhase());
    }

    @Test
    void boardStillInProgressAfterSixPuts_noWin() {
        // X: (1,1),(2,1),(3,1) — that's a column win; avoid that
        // X: (1,1),(2,2),(3,1) — X row1, row2, row3 col1+col3; let's use scattered puts
        ThreePiecesBoard board = scatteredSixPuts();
        assertEquals(State.IN_PROGRESS, board.state());
    }

    // -------------------------------------------------------------------------
    // Win during put phase
    // -------------------------------------------------------------------------

    @Test
    void xWinsDuringPutPhaseOnRow() {
        // X: (1,1),(1,2),(1,3); O: (2,1),(2,2)
        ThreePiecesBoard board = new ThreePiecesBoard()
                .place(1, 1).place(2, 1)
                .place(1, 2).place(2, 2)
                .place(1, 3); // X completes row 1 — wins on 5th put
        assertEquals(State.X_WINS, board.state());
    }

    @Test
    void oWinsDuringPutPhaseOnColumn() {
        // O wins column 3: O at (1,3),(2,3),(3,3); X at (1,1),(2,1),(3,2)
        ThreePiecesBoard board = new ThreePiecesBoard()
                .place(1, 1).place(1, 3)
                .place(2, 1).place(2, 3)
                .place(3, 2).place(3, 3); // O wins
        assertEquals(State.O_WINS, board.state());
    }

    // -------------------------------------------------------------------------
    // Move phase: basic valid move
    // -------------------------------------------------------------------------

    @Test
    void validMoveChangesBoardCorrectly() {
        ThreePiecesBoard board = scatteredSixPuts();
        // X at (1,1),(2,3),(3,2); O at (1,3),(2,1),(3,1); empty: (1,2),(2,2),(3,3)
        assertEquals(Color.X, board.currentPlayer());
        // Move X's piece from (1,1) to (1,2) (empty)
        ThreePiecesBoard after = board.move(1, 1, 1, 2);
        assertNull(after.at(1, 1));
        assertEquals(Color.X, after.at(1, 2));
    }

    @Test
    void afterValidMoveTurnSwitches() {
        ThreePiecesBoard board = scatteredSixPuts();
        // Move X from (1,1) to (1,2) (empty)
        ThreePiecesBoard after = board.move(1, 1, 1, 2);
        assertEquals(Color.O, after.currentPlayer());
    }

    @Test
    void movePhasePersistsAsMovePhaseFlagAfterMove() {
        ThreePiecesBoard board = scatteredSixPuts().move(1, 1, 1, 2);
        assertFalse(board.isPutPhase());
    }

    // -------------------------------------------------------------------------
    // Move phase: invalid moves
    // -------------------------------------------------------------------------

    @Test
    void cannotMoveFromEmptySquare() {
        ThreePiecesBoard board = scatteredSixPuts();
        // (1,2) is empty; (2,2) is also empty — cannot move from empty
        assertThrows(IllegalArgumentException.class, () -> board.move(1, 2, 2, 2));
    }

    @Test
    void cannotMoveOpponentsPiece() {
        ThreePiecesBoard board = scatteredSixPuts();
        // O is at (1,3); it's X's turn — cannot move O's piece
        assertThrows(IllegalArgumentException.class, () -> board.move(1, 3, 1, 2));
    }

    @Test
    void cannotMoveToOccupiedSquare() {
        ThreePiecesBoard board = scatteredSixPuts();
        // X at (1,1); (2,3) also X — cannot move X to another X square
        assertThrows(IllegalArgumentException.class, () -> board.move(1, 1, 2, 3));
    }

    @Test
    void cannotMoveWithCoordinatesOutOfRange() {
        ThreePiecesBoard board = scatteredSixPuts();
        assertThrows(IllegalArgumentException.class, () -> board.move(0, 1, 1, 3));
        assertThrows(IllegalArgumentException.class, () -> board.move(1, 1, 4, 1));
    }

    @Test
    void cannotPutDuringMovePhase() {
        ThreePiecesBoard board = scatteredSixPuts();
        assertThrows(IllegalArgumentException.class, () -> board.place(1, 3));
    }

    @Test
    void cannotPutAfterGameOver() {
        ThreePiecesBoard board = new ThreePiecesBoard()
                .place(1, 1).place(2, 1)
                .place(1, 2).place(2, 2)
                .place(1, 3); // X wins
        assertThrows(IllegalArgumentException.class, () -> board.place(3, 3));
    }

    // -------------------------------------------------------------------------
    // Win during move phase
    // -------------------------------------------------------------------------

    @Test
    void xWinsByMoveCompletingRow() {
        // After scattered six puts: X at (1,1),(2,1),(3,1); O at (1,2),(2,2),(3,2)
        // X moves (3,1) to (1,3) → X has (1,1),(2,1),(1,3) — no win
        // X moves (1,1) to (1,2) — blocked
        // Let's set up: X at (1,2),(1,3),(2,1); O at (2,2),(2,3),(3,1)
        // X moves (2,1) to (1,1) → X has (1,1),(1,2),(1,3) → wins row 1
        ThreePiecesBoard board = new ThreePiecesBoard()
                .place(1, 2).place(2, 2)
                .place(1, 3).place(2, 3)
                .place(2, 1).place(3, 1); // X: (1,2),(1,3),(2,1); O: (2,2),(2,3),(3,1); X's turn in move phase
        assertFalse(board.isPutPhase());
        assertEquals(Color.X, board.currentPlayer());
        ThreePiecesBoard after = board.move(2, 1, 1, 1); // X: (1,1),(1,2),(1,3)
        assertEquals(State.X_WINS, after.state());
    }

    @Test
    void noDrawInVariant_gameAlwaysContinues() {
        // In three-pieces variant there is no draw; the game must not produce DRAW state
        // Play 6 puts that result in no winner, then verify still IN_PROGRESS
        ThreePiecesBoard board = scatteredSixPuts();
        assertEquals(State.IN_PROGRESS, board.state());
    }

    // -------------------------------------------------------------------------
    // Immutability
    // -------------------------------------------------------------------------

    @Test
    void placeDoesNotMutateOriginal() {
        ThreePiecesBoard original = new ThreePiecesBoard();
        original.place(2, 2);
        assertNull(original.at(2, 2));
    }

    @Test
    void moveDoesNotMutateOriginal() {
        ThreePiecesBoard board = scatteredSixPuts();
        // Move X from (1,1) to empty (1,2)
        board.move(1, 1, 1, 2);
        assertEquals(Color.X, board.at(1, 1)); // unchanged
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Plays 6 puts with no winner:
     * X: (1,1),(2,2),(3,3)  — that's the main diagonal; avoid it.
     * Use: X: (1,1),(2,3),(3,2)   O: (1,3),(2,1),(3,1)
     * Sequence: X(1,1),O(1,3), X(2,3),O(2,1), X(3,2),O(3,1)
     * It's X's turn in move phase.
     */
    private static ThreePiecesBoard scatteredSixPuts() {
        return new ThreePiecesBoard()
                .place(1, 1).place(1, 3)
                .place(2, 3).place(2, 1)
                .place(3, 2).place(3, 1);
    }

    /**
     * Plays 6 puts resulting in board entry to move phase (no win during puts).
     */
    private static ThreePiecesBoard sixPuts() {
        return scatteredSixPuts();
    }
}
