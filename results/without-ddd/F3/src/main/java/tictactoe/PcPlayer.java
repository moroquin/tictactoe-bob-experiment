package tictactoe;

/**
 * Deterministic PC player (plays as O).
 * Chooses the target square using these rules in order:
 *  1. Win:    complete three in a line with one put.
 *  2. Block:  prevent X from completing three in a line.
 *  3. Center: (2,2) if empty.
 *  4. Corner: first empty corner in order (1,1),(1,3),(3,1),(3,3).
 *  5. Any:    first empty square in reading order.
 * When rules 1 or 2 match more than one square, the first in reading order is used.
 */
final class PcPlayer {

    private PcPlayer() {}

    /** Returns a new Board after the PC places its piece. */
    static Board play(Board board) {
        int[] move = chooseMove(board);
        return board.place(move[0], move[1]);
    }

    static int[] chooseMove(Board board) {
        Color pc = board.currentPlayer(); // O
        Color human = pc.other();          // X

        // Rule 1: Win
        int[] win = findWinningMove(board, pc);
        if (win != null) return win;

        // Rule 2: Block
        int[] block = findWinningMove(board, human);
        if (block != null) return block;

        // Rule 3: Center
        if (board.at(2, 2) == null) return new int[]{2, 2};

        // Rule 4: Corner
        int[][] corners = {{1, 1}, {1, 3}, {3, 1}, {3, 3}};
        for (int[] corner : corners) {
            if (board.at(corner[0], corner[1]) == null) return corner;
        }

        // Rule 5: First empty in reading order
        for (int r = 1; r <= 3; r++) {
            for (int c = 1; c <= 3; c++) {
                if (board.at(r, c) == null) return new int[]{r, c};
            }
        }

        throw new IllegalStateException("No empty square found");
    }

    /**
     * Finds the first square (reading order) where placing 'color' would complete
     * three in a line. Returns null if no such square exists.
     */
    private static int[] findWinningMove(Board board, Color color) {
        for (int r = 1; r <= 3; r++) {
            for (int c = 1; c <= 3; c++) {
                if (board.at(r, c) == null && wouldWin(board, r, c, color)) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    /**
     * Returns true if placing 'color' at (row, col) would complete three in a line.
     * Uses Board.place to leverage existing win detection.
     */
    private static boolean wouldWin(Board board, int row, int col, Color color) {
        // We need to check if placing 'color' at (row,col) wins.
        // Board.place uses currentPlayer(); we need to check for an arbitrary color.
        // Simulate: if the current player IS color, just place and check.
        // If not, we cannot use Board.place directly for the other color.
        // Instead, perform the win check manually on a temporary grid.
        Color[][] sim = copyBoard(board);
        sim[row - 1][col - 1] = color;
        return hasWon(sim, color);
    }

    private static Color[][] copyBoard(Board board) {
        Color[][] grid = new Color[3][3];
        for (int r = 1; r <= 3; r++) {
            for (int c = 1; c <= 3; c++) {
                grid[r - 1][c - 1] = board.at(r, c);
            }
        }
        return grid;
    }

    private static boolean hasWon(Color[][] sq, Color c) {
        for (int r = 0; r < 3; r++) {
            if (sq[r][0] == c && sq[r][1] == c && sq[r][2] == c) return true;
        }
        for (int col = 0; col < 3; col++) {
            if (sq[0][col] == c && sq[1][col] == c && sq[2][col] == c) return true;
        }
        if (sq[0][0] == c && sq[1][1] == c && sq[2][2] == c) return true;
        if (sq[0][2] == c && sq[1][1] == c && sq[2][0] == c) return true;
        return false;
    }
}
