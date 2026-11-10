package tictactoe;

/**
 * Three-Pieces variant: each player has exactly 3 pieces.
 * Rounds 1-6 are puts; from round 7 the current player moves one of their
 * own pieces to an empty square. There is no draw: play continues until
 * someone wins.
 */
public class ThreePiecesGame {

    public enum Status { ONGOING, X_WINS, O_WINS }

    private static final int PIECES_PER_PLAYER = 3;

    private final Board board = new Board();
    private char currentPlayer = 'X';
    private Status status = Status.ONGOING;

    /** Pieces already placed by each player (index 0 = X, 1 = O). */
    private final int[] placed = new int[2];

    public char currentPlayer() { return currentPlayer; }
    public Status status()      { return status; }
    public Board board()        { return board; }
    public boolean isOver()     { return status != Status.ONGOING; }

    /** True while the current player still has pieces left to place. */
    public boolean isPutPhase() {
        return placed[playerIndex(currentPlayer)] < PIECES_PER_PLAYER;
    }

    /**
     * Attempt to place a new piece for the current player.
     * Valid only while the player still has pieces to place.
     *
     * @return true if accepted, false if invalid.
     */
    public boolean tryPut(int row, int col) {
        if (!isPutPhase()) return false;
        if (row < 1 || row > 3 || col < 1 || col > 3) return false;
        if (!board.isEmpty(row, col)) return false;

        board.place(row, col, currentPlayer);
        placed[playerIndex(currentPlayer)]++;
        return updateStatusOrSwitch();
    }

    /**
     * Attempt to move one of the current player's pieces to an empty square.
     * Valid only when the current player has already placed all their pieces.
     *
     * @return true if accepted, false if invalid.
     */
    public boolean tryMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (isPutPhase()) return false;
        if (fromRow < 1 || fromRow > 3 || fromCol < 1 || fromCol > 3) return false;
        if (toRow < 1 || toRow > 3 || toCol < 1 || toCol > 3) return false;
        if (board.get(fromRow, fromCol) != currentPlayer) return false;
        if (!board.isEmpty(toRow, toCol)) return false;

        board.place(fromRow, fromCol, '.');
        board.place(toRow, toCol, currentPlayer);
        return updateStatusOrSwitch();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private boolean updateStatusOrSwitch() {
        if (board.hasWon(currentPlayer)) {
            status = (currentPlayer == 'X') ? Status.X_WINS : Status.O_WINS;
        } else {
            currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
        }
        return true;
    }

    private static int playerIndex(char player) {
        return player == 'X' ? 0 : 1;
    }
}
