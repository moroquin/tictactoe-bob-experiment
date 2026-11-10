package tictactoe.domain;

import java.util.List;

/**
 * A PC Player: controlled by the program. Uses a fixed, deterministic strategy (D12):
 *   1. Win:        complete a line with one put for itself.
 *   2. Block:      prevent the opponent from completing a line.
 *   3. Center:     put at (2,2) if empty.
 *   4. Corner:     first empty corner in reading order: (1,1),(1,3),(3,1),(3,3).
 *   5. First empty: first empty Square in reading order.
 * When rules 1 or 2 match more than one Square, the first one in reading order is used.
 */
public final class PcPlayer extends Player {

    public PcPlayer(Color color) {
        super(color);
    }

    /**
     * Applies the PC strategy to choose a Coordinate. Never returns null: there is always
     * at least one empty Square when the game is not over.
     */
    @Override
    public Coordinate chooseCoordinate(TicTacToe game) {
        Color opponentColor = (color() == Color.X) ? Color.O : Color.X;

        // Rule 1: Win — find a square where placing own color completes a line
        Coordinate win = findThreat(game, color());
        if (win != null) return win;

        // Rule 2: Block — find a square where the opponent would complete a line
        Coordinate block = findThreat(game, opponentColor);
        if (block != null) return block;

        // Rule 3: Center
        Coordinate center = new Coordinate(2, 2);
        if (game.colorAt(center) == Color.EMPTY) return center;

        // Rule 4: Corner — first empty in reading order among corners
        for (Coordinate corner : List.of(
                new Coordinate(1, 1),
                new Coordinate(1, 3),
                new Coordinate(3, 1),
                new Coordinate(3, 3))) {
            if (game.colorAt(corner) == Color.EMPTY) return corner;
        }

        // Rule 5: First empty square in reading order
        for (Coordinate c : Coordinate.all()) {
            if (game.colorAt(c) == Color.EMPTY) return c;
        }

        // Should never happen: game.put() guards against calling this when the game is over
        throw new IllegalStateException("No empty square available");
    }

    /**
     * Finds the first empty Coordinate (in reading order) that, if filled with targetColor,
     * would complete a Line. Returns null if no such square exists.
     *
     * A Line is a threat for targetColor when exactly two of its three Coordinates hold
     * targetColor and the third is empty.
     */
    private Coordinate findThreat(TicTacToe game, Color targetColor) {
        // Collect candidates: one empty square in a line where the other two are targetColor.
        // We want the first candidate in reading order.
        Coordinate best = null;
        for (Line line : Line.all()) {
            List<Coordinate> coords = line.coordinates();
            int count = 0;
            Coordinate empty = null;
            for (Coordinate c : coords) {
                Color here = game.colorAt(c);
                if (here == targetColor) {
                    count++;
                } else if (here == Color.EMPTY) {
                    empty = c;
                }
            }
            if (count == 2 && empty != null) {
                // This line is a threat; take the first candidate in reading order
                if (best == null || readingOrderIndex(empty) < readingOrderIndex(best)) {
                    best = empty;
                }
            }
        }
        return best;
    }

    /** Returns the reading-order index (0–8) of a Coordinate: (row-1)*3 + (column-1). */
    private static int readingOrderIndex(Coordinate c) {
        return (c.row() - 1) * 3 + (c.column() - 1);
    }
}
