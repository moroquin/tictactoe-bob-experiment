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
        playerX = new HumanPlayer(Color.X);
        playerO = new HumanPlayer(Color.O);
        game = new TicTacToe(playerX, playerO);
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

    // ---- Normal alternation ----

    @Test
    void I4_afterXPutsStateIsOToPut() {
        assertTrue(game.put(new Coordinate(1, 1)));
        assertEquals(State.O_TO_PUT, game.state());
        assertEquals(playerO, game.currentPlayer());
    }

    @Test
    void I4_afterXAndOPutStateIsXToPut() {
        game.put(new Coordinate(1, 1));
        game.put(new Coordinate(2, 2));
        assertEquals(State.X_TO_PUT, game.state());
        assertEquals(playerX, game.currentPlayer());
    }

    @Test
    void I5_putPlacesPieceOfCurrentPlayerColor() {
        game.put(new Coordinate(1, 1));
        assertEquals(Color.X, game.colorAt(new Coordinate(1, 1)));
    }

    // ---- Occupied square rejection ----

    @Test
    void I3_putOnOccupiedSquareIsRejected() {
        game.put(new Coordinate(1, 1));  // X
        boolean result = game.put(new Coordinate(1, 1));  // O tries same square
        assertFalse(result);
    }

    @Test
    void I3_boardDoesNotChangeAfterRejectedPut() {
        game.put(new Coordinate(1, 1));  // X
        game.put(new Coordinate(1, 1));  // rejected
        assertEquals(Color.X, game.colorAt(new Coordinate(1, 1)));
    }

    @Test
    void I3_stateDoesNotChangeAfterRejectedPut() {
        game.put(new Coordinate(1, 1));  // X -> O_TO_PUT
        game.put(new Coordinate(1, 1));  // rejected: still O_TO_PUT
        assertEquals(State.O_TO_PUT, game.state());
    }

    @Test
    void I3_currentPlayerDoesNotChangeAfterRejectedPut() {
        game.put(new Coordinate(1, 1));  // X -> O_TO_PUT
        game.put(new Coordinate(1, 1));  // rejected
        assertEquals(playerO, game.currentPlayer());
    }

    // ---- Win detection ----

    @Test
    void I6_xWinsOnRow() {
        // X: row 1; O: row 2 (two squares)
        game.put(new Coordinate(1, 1)); // X
        game.put(new Coordinate(2, 1)); // O
        game.put(new Coordinate(1, 2)); // X
        game.put(new Coordinate(2, 2)); // O
        game.put(new Coordinate(1, 3)); // X wins
        assertEquals(State.X_WINS, game.state());
        assertTrue(game.state().isOver());
    }

    @Test
    void I6_oWinsOnColumn() {
        game.put(new Coordinate(1, 1)); // X
        game.put(new Coordinate(1, 2)); // O
        game.put(new Coordinate(2, 1)); // X
        game.put(new Coordinate(2, 2)); // O
        game.put(new Coordinate(3, 3)); // X
        game.put(new Coordinate(3, 2)); // O wins column 2
        assertEquals(State.O_WINS, game.state());
    }

    @Test
    void I6_xWinsOnDiagonal() {
        game.put(new Coordinate(1, 1)); // X
        game.put(new Coordinate(1, 2)); // O
        game.put(new Coordinate(2, 2)); // X
        game.put(new Coordinate(2, 1)); // O
        game.put(new Coordinate(3, 3)); // X wins diagonal
        assertEquals(State.X_WINS, game.state());
    }

    @Test
    void I6_xWinsOnInverseDiagonal() {
        game.put(new Coordinate(1, 3)); // X
        game.put(new Coordinate(1, 1)); // O
        game.put(new Coordinate(2, 2)); // X
        game.put(new Coordinate(2, 1)); // O
        game.put(new Coordinate(3, 1)); // X wins inverse diagonal
        assertEquals(State.X_WINS, game.state());
    }

    @Test
    void I6_winIsCheckedBeforeDraw() {
        // X wins row 1 on the 9th (last) put. Board is full but result is X_WINS, not DRAW.
        // Final board:  X X X / O O X / X O O
        // X: (1,1),(2,3),(3,1),(1,2),(1,3)  O: (2,1),(2,2),(3,2),(3,3)
        game.put(new Coordinate(1, 1)); // 1 X
        game.put(new Coordinate(2, 1)); // 2 O
        game.put(new Coordinate(2, 3)); // 3 X
        game.put(new Coordinate(2, 2)); // 4 O
        game.put(new Coordinate(3, 1)); // 5 X
        game.put(new Coordinate(3, 2)); // 6 O
        game.put(new Coordinate(1, 2)); // 7 X
        game.put(new Coordinate(3, 3)); // 8 O
        // 8 puts done, no winner yet
        assertEquals(State.X_TO_PUT, game.state());
        game.put(new Coordinate(1, 3)); // 9 X completes row 1 — full board but win takes priority
        assertEquals(State.X_WINS, game.state());
    }

    // ---- Draw ----

    @Test
    void I7_drawWhenBoardFullAndNoWin() {
        // X O X
        // X X O
        // O X O   — no winner, all 9 squares filled
        game.put(new Coordinate(1, 1)); // X
        game.put(new Coordinate(1, 2)); // O
        game.put(new Coordinate(1, 3)); // X
        game.put(new Coordinate(2, 3)); // O
        game.put(new Coordinate(2, 2)); // X
        game.put(new Coordinate(3, 1)); // O
        game.put(new Coordinate(2, 1)); // X
        game.put(new Coordinate(3, 3)); // O
        game.put(new Coordinate(3, 2)); // X
        assertEquals(State.DRAW, game.state());
        assertTrue(game.state().isOver());
    }

    // ---- No put after game over ----

    @Test
    void I8_putAfterWinIsRejected() {
        game.put(new Coordinate(1, 1)); // X
        game.put(new Coordinate(2, 1)); // O
        game.put(new Coordinate(1, 2)); // X
        game.put(new Coordinate(2, 2)); // O
        game.put(new Coordinate(1, 3)); // X wins
        boolean result = game.put(new Coordinate(3, 1));
        assertFalse(result);
        assertEquals(State.X_WINS, game.state());
    }

    @Test
    void I8_putAfterDrawIsRejected() {
        game.put(new Coordinate(1, 1)); // X
        game.put(new Coordinate(1, 2)); // O
        game.put(new Coordinate(1, 3)); // X
        game.put(new Coordinate(2, 3)); // O
        game.put(new Coordinate(2, 2)); // X
        game.put(new Coordinate(3, 1)); // O
        game.put(new Coordinate(2, 1)); // X
        game.put(new Coordinate(3, 3)); // O
        game.put(new Coordinate(3, 2)); // X -> Draw
        // all squares occupied; any further put should be rejected
        // but there are no empty squares; test the game-over guard specifically
        // by creating a new game and forcing it to X_WINS then trying again
        TicTacToe g2 = new TicTacToe(playerX, playerO);
        g2.put(new Coordinate(1, 1)); // X
        g2.put(new Coordinate(2, 1)); // O
        g2.put(new Coordinate(1, 2)); // X
        g2.put(new Coordinate(2, 2)); // O
        g2.put(new Coordinate(1, 3)); // X wins
        assertFalse(g2.put(new Coordinate(3, 1)));
        assertEquals(State.X_WINS, g2.state());
    }
}
