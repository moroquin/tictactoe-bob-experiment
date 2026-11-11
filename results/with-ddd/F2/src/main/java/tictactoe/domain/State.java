package tictactoe.domain;

/**
 * The state of a TicTacToe game.
 */
public enum State {
    X_TO_PUT, O_TO_PUT, X_WINS, O_WINS, DRAW;

    /** Returns true when the game is over (X wins, O wins, or Draw). */
    public boolean isOver() {
        return this == X_WINS || this == O_WINS || this == DRAW;
    }
}
