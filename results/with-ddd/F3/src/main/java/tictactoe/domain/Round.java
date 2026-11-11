package tictactoe.domain;

/**
 * Decides which Player puts next: X always starts, then Players alternate.
 * Invariant I4: X always starts; the Round alternates X, O, X, O…
 */
final class Round {

    private final Player playerX;
    private final Player playerO;
    private Player current;

    Round(Player playerX, Player playerO) {
        this.playerX = playerX;
        this.playerO = playerO;
        this.current = playerX; // X always starts (I4)
    }

    /** Returns the Player who puts next. */
    public Player current() {
        return current;
    }

    /**
     * Advances to the next Player.
     * Package-private: only TicTacToe may call this.
     */
    void next() {
        current = (current == playerX) ? playerO : playerX;
    }
}
