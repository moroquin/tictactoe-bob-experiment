package tictactoe.domain;

/** Where a game is: X to put, O to put, X to move, O to move, X wins, O wins or Draw. */
public enum State {
    X_TO_PUT,
    O_TO_PUT,
    X_TO_MOVE,
    O_TO_MOVE,
    X_WINS,
    O_WINS,
    DRAW;

    /** Returns true when the game is over: X wins, O wins or Draw. */
    public boolean isOver() {
        return this == X_WINS || this == O_WINS || this == DRAW;
    }

    /** Returns true when the game is in the move phase: X to move or O to move. */
    public boolean isMoving() {
        return this == X_TO_MOVE || this == O_TO_MOVE;
    }
}
