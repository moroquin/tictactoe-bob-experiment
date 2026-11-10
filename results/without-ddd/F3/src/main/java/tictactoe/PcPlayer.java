package tictactoe;

/**
 * A PC-controlled player that chooses its move deterministically:
 *  1. Win:    complete three in a line if possible (reading order tie-break).
 *  2. Block:  prevent the opponent from completing three in a line.
 *  3. Center: take (2,2) if empty.
 *  4. Corner: first empty corner in order (1,1),(1,3),(3,1),(3,3).
 *  5. Any:    first empty square in reading order.
 */
public class PcPlayer {

    /** All lines on the board (rows, columns, diagonals). */
    private static final int[][][] LINES = {
        {{1,1},{1,2},{1,3}},
        {{2,1},{2,2},{2,3}},
        {{3,1},{3,2},{3,3}},
        {{1,1},{2,1},{3,1}},
        {{1,2},{2,2},{3,2}},
        {{1,3},{2,3},{3,3}},
        {{1,1},{2,2},{3,3}},
        {{1,3},{2,2},{3,1}},
    };

    private static final int[][] CORNERS = {{1,1},{1,3},{3,1},{3,3}};

    /**
     * Returns the [row, col] the PC wants to put on, given the board and
     * the PC's own piece ('O' when X plays first).
     */
    public int[] chooseMove(Board board, char myPiece) {
        char opponentPiece = (myPiece == 'O') ? 'X' : 'O';

        // Rule 1: win
        int[] win = findWinningMove(board, myPiece);
        if (win != null) return win;

        // Rule 2: block
        int[] block = findWinningMove(board, opponentPiece);
        if (block != null) return block;

        // Rule 3: center
        if (board.isEmpty(2, 2)) return new int[]{2, 2};

        // Rule 4: first empty corner
        for (int[] corner : CORNERS) {
            if (board.isEmpty(corner[0], corner[1])) return corner;
        }

        // Rule 5: first empty square in reading order
        for (int r = 1; r <= 3; r++)
            for (int c = 1; c <= 3; c++)
                if (board.isEmpty(r, c)) return new int[]{r, c};

        throw new IllegalStateException("No empty square found — board is full");
    }

    /**
     * Returns the first empty square (in reading order) that, when filled with
     * {@code piece}, would complete three in a line. Returns null if none.
     */
    private int[] findWinningMove(Board board, char piece) {
        for (int[][] line : LINES) {
            int pieceCount = 0;
            int[] empty = null;
            for (int[] sq : line) {
                char cell = board.get(sq[0], sq[1]);
                if (cell == piece) {
                    pieceCount++;
                } else if (cell == '.') {
                    if (empty == null) empty = sq; // first empty in line
                } else {
                    pieceCount = -9; // opponent piece blocks this line
                    break;
                }
            }
            // Two of our pieces and exactly one empty square → winning move
            if (pieceCount == 2 && empty != null) return empty;
        }
        return null;
    }
}
