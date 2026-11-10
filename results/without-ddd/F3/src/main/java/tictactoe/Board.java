package tictactoe;

/**
 * The 3×3 board. Rows and columns are 1-based externally; 0-based internally.
 */
public class Board {

    private final char[][] cells = new char[3][3];

    public Board() {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                cells[r][c] = '.';
    }

    /** Returns true when the square (row, col) — 1-based — is empty. */
    public boolean isEmpty(int row, int col) {
        return cells[row - 1][col - 1] == '.';
    }

    /** Places {@code piece} on (row, col) — 1-based. */
    public void place(int row, int col, char piece) {
        cells[row - 1][col - 1] = piece;
    }

    /** Returns the piece at (row, col) — 1-based — or '.' if empty. */
    public char get(int row, int col) {
        return cells[row - 1][col - 1];
    }

    /** Returns true when {@code piece} has three in a row, column, or diagonal. */
    public boolean hasWon(char piece) {
        // rows
        for (int r = 1; r <= 3; r++)
            if (get(r, 1) == piece && get(r, 2) == piece && get(r, 3) == piece)
                return true;
        // columns
        for (int c = 1; c <= 3; c++)
            if (get(1, c) == piece && get(2, c) == piece && get(3, c) == piece)
                return true;
        // main diagonal
        if (get(1, 1) == piece && get(2, 2) == piece && get(3, 3) == piece)
            return true;
        // anti-diagonal
        if (get(1, 3) == piece && get(2, 2) == piece && get(3, 1) == piece)
            return true;
        return false;
    }

    /** Returns true when all 9 squares are occupied. */
    public boolean isFull() {
        for (int r = 1; r <= 3; r++)
            for (int c = 1; c <= 3; c++)
                if (isEmpty(r, c)) return false;
        return true;
    }

    /** Returns three lines representing the board, one per row. */
    public String render() {
        StringBuilder sb = new StringBuilder();
        for (int r = 1; r <= 3; r++) {
            sb.append(get(r, 1)).append(get(r, 2)).append(get(r, 3));
            if (r < 3) sb.append('\n');
        }
        return sb.toString();
    }
}
