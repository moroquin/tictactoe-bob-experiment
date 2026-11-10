package tictactoe.domain;

/**
 * Decides which Player puts next. X always starts; then the Players alternate (I4).
 * next() is package-private: only TicTacToe may advance the Round (I10).
 */
public final class Round {

    private final Player playerX;
    private final Player playerO;
    private Player current;

    Round(Player playerX, Player playerO) {
        this.playerX = playerX;
        this.playerO = playerO;
        this.current = playerX;  // X always starts (I4)
    }

    public Player current() {
        return current;
    }

    /**
     * Advances to the next Player.
     * Package-private: only TicTacToe may call this (I10).
     */
    void next() {
        current = (current == playerX) ? playerO : playerX;
    }
}
