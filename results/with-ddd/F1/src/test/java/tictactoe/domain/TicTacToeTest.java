package tictactoe.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicTacToeTest {

    private Player playerX;
    private Player playerO;
    private TicTacToe game;

    @BeforeEach
    void setUp() {
        playerX = new Player(Color.X);
        playerO = new Player(Color.O);
        game = new TicTacToe(playerX, playerO);
    }

    // ── Initial state ────────────────────────────────────────────────────────

    @Test
    void I4_initialStateIsXToPut() {
        assertEquals(State.X_TO_PUT, game.state());
    }

    @Test
    void I4_initialCurrentPlayerIsX() {
        assertEquals(playerX, game.currentPlayer());
    }

    @Test
    void I1_initialBoardIsAllEmpty() {
        for (Coordinate c : Coordinate.all()) {
            assertEquals(Color.EMPTY, game.colorAt(c));
        }
    }

    // ── Valid puts ───────────────────────────────────────────────────────────

    @Test
    void I5_xPutPlacesXPiece() {
        game.put(new Coordinate(1, 1));
        assertEquals(Color.X, game.colorAt(new Coordinate(1, 1)));
    }

    @Test
    void I4_afterXPutCurrentPlayerIsO() {
        game.put(new Coordinate(1, 1));
        assertEquals(playerO, game.currentPlayer());
        assertEquals(State.O_TO_PUT, game.state());
    }

    @Test
    void I5_oPutPlacesOPiece() {
        game.put(new Coordinate(1, 1));
        game.put(new Coordinate(2, 2));
        assertEquals(Color.O, game.colorAt(new Coordinate(2, 2)));
    }

    @Test
    void validPutReturnsTrue() {
        assertTrue(game.put(new Coordinate(1, 1)));
    }

    // ── Occupied square rejection ─────────────────────────────────────────────

    @Test
    void I3_putOnOccupiedSquareReturnsFalse() {
        game.put(new Coordinate(1, 1));
        assertFalse(game.put(new Coordinate(1, 1)));
    }

    @Test
    void I3_putOnOccupiedSquareDoesNotChangeBoardOrState() {
        game.put(new Coordinate(1, 1));         // X at (1,1), state=O_TO_PUT
        game.put(new Coordinate(1, 1));         // rejected
        assertEquals(State.O_TO_PUT, game.state());
        assertEquals(Color.X, game.colorAt(new Coordinate(1, 1)));
    }

    @Test
    void I3_putOnOccupiedSquareDoesNotAdvanceRound() {
        game.put(new Coordinate(1, 1));         // X at (1,1)
        game.put(new Coordinate(1, 1));         // rejected, still O's turn
        assertEquals(playerO, game.currentPlayer());
    }

    // ── Win detection ─────────────────────────────────────────────────────────

    @Test
    void I6_xWinsOnFirstRow() {
        // X: (1,1) (1,2) (1,3) — O: (2,1) (2,2)
        game.put(new Coordinate(1, 1));
        game.put(new Coordinate(2, 1));
        game.put(new Coordinate(1, 2));
        game.put(new Coordinate(2, 2));
        game.put(new Coordinate(1, 3));
        assertEquals(State.X_WINS, game.state());
    }

    @Test
    void I6_oWinsOnSecondColumn() {
        // X: (1,1) (2,1) (3,1) — O: (1,2) (2,2) (3,2)
        game.put(new Coordinate(1, 1));
        game.put(new Coordinate(1, 2));
        game.put(new Coordinate(2, 1));
        game.put(new Coordinate(2, 2));
        game.put(new Coordinate(3, 3)); // X to a non-column-2 square
        game.put(new Coordinate(3, 2)); // O completes column 2
        assertEquals(State.O_WINS, game.state());
    }

    @Test
    void I6_xWinsOnDiagonal() {
        // X: (1,1) (2,2) (3,3) — O: (1,2) (1,3)
        game.put(new Coordinate(1, 1));
        game.put(new Coordinate(1, 2));
        game.put(new Coordinate(2, 2));
        game.put(new Coordinate(1, 3));
        game.put(new Coordinate(3, 3));
        assertEquals(State.X_WINS, game.state());
    }

    @Test
    void I6_xWinsOnInverseDiagonal() {
        // X: (1,3) (2,2) (3,1) — O: (1,1) (1,2)
        game.put(new Coordinate(1, 3));
        game.put(new Coordinate(1, 1));
        game.put(new Coordinate(2, 2));
        game.put(new Coordinate(1, 2));
        game.put(new Coordinate(3, 1));
        assertEquals(State.X_WINS, game.state());
    }

    // ── Draw detection ────────────────────────────────────────────────────────

    @Test
    void I7_drawWhenBoardFullAndNoWin() {
        // Fill board without any win:
        // X O X
        // X X O
        // O X O
        game.put(new Coordinate(1, 1)); // X
        game.put(new Coordinate(1, 2)); // O
        game.put(new Coordinate(1, 3)); // X
        game.put(new Coordinate(2, 3)); // O
        game.put(new Coordinate(2, 2)); // X
        game.put(new Coordinate(3, 1)); // O
        game.put(new Coordinate(2, 1)); // X
        game.put(new Coordinate(3, 3)); // O
        game.put(new Coordinate(3, 2)); // X — board full, no win
        assertEquals(State.DRAW, game.state());
    }

    // ── Game over: no more puts ────────────────────────────────────────────────

    @Test
    void I8_putAfterXWinsReturnsFalse() {
        // X wins first row
        game.put(new Coordinate(1, 1));
        game.put(new Coordinate(2, 1));
        game.put(new Coordinate(1, 2));
        game.put(new Coordinate(2, 2));
        game.put(new Coordinate(1, 3));
        assertEquals(State.X_WINS, game.state());
        assertFalse(game.put(new Coordinate(3, 1)));
    }

    @Test
    void I8_putAfterDrawReturnsFalse() {
        // Fill board without any win (same sequence as drawWhenBoardFullAndNoWin)
        game.put(new Coordinate(1, 1));
        game.put(new Coordinate(1, 2));
        game.put(new Coordinate(1, 3));
        game.put(new Coordinate(2, 3));
        game.put(new Coordinate(2, 2));
        game.put(new Coordinate(3, 1));
        game.put(new Coordinate(2, 1));
        game.put(new Coordinate(3, 3));
        game.put(new Coordinate(3, 2));
        assertEquals(State.DRAW, game.state());
        // No empty square; pass an already-occupied one
        assertFalse(game.put(new Coordinate(1, 1)));
    }

    // ── State.isOver ──────────────────────────────────────────────────────────

    @Test
    void stateIsOverFalseWhileInProgress() {
        assertFalse(game.state().isOver());
        game.put(new Coordinate(1, 1));
        assertFalse(game.state().isOver());
    }

    @Test
    void stateIsOverTrueAfterWin() {
        game.put(new Coordinate(1, 1));
        game.put(new Coordinate(2, 1));
        game.put(new Coordinate(1, 2));
        game.put(new Coordinate(2, 2));
        game.put(new Coordinate(1, 3));
        assertTrue(game.state().isOver());
    }
}
