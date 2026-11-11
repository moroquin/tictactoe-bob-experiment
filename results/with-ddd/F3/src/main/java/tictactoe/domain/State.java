package tictactoe.domain;

/**
 * The state of a TicTacToe game.
 * CR-2 adds X_TO_MOVE and O_TO_MOVE for the three-pieces variant (D24).
 */
public enum State {
    X_TO_PUT, O_TO_PUT, X_TO_MOVE, O_TO_MOVE, X_WINS, O_WINS, DRAW;

    /** Returns true when the game is over (X wins, O wins, or Draw). */
    public boolean isOver() {
        return this == X_WINS || this == O_WINS || this == DRAW;
    }

    /** Returns true when the game is in the move phase (X_TO_MOVE or O_TO_MOVE). */
    public boolean isMoving() {
        return this == X_TO_MOVE || this == O_TO_MOVE;
    }
}
