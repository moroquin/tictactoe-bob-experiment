package tictactoe.domain;

import java.util.List;

/**
 * A Player controlled by the program. It chooses its Coordinate automatically
 * using five fixed rules in priority order (D18):
 *
 *  1. Win:    complete three in a line for the PC's own Color.
 *  2. Block:  prevent the opponent from completing three in a line.
 *  3. Center: (2,2) if it is empty.
 *  4. Corner: first empty corner in order (1,1), (1,3), (3,1), (3,3).
 *  5. Any:    first empty Square in reading order.
 *
 * Ties in rules 1 and 2 are broken by reading order.
 * The PC is deterministic: no randomness, no AI, no external services.
 */
public final class PcPlayer extends Player {

    private static final List<Coordinate> CORNERS = List.of(
            new Coordinate(1, 1),
            new Coordinate(1, 3),
            new Coordinate(3, 1),
            new Coordinate(3, 3)
    );

    /**
     * Creates a PcPlayer.
     *
     * @param color the Color of this player (X or O)
     */
    public PcPlayer(Color color) {
        super(color);
    }

    /**
     * Chooses the Coordinate for the next Put using the five fixed rules.
     */
    @Override
    public Coordinate chooseCoordinate(TicTacToe game) {
        // Rule 1: Win — complete three in a line for my own Color.
        Coordinate win = firstWinningSquare(game, color());
        if (win != null) return win;

        // Rule 2: Block — prevent the opponent from completing three in a line.
        Color opponent = (color() == Color.X) ? Color.O : Color.X;
        Coordinate block = firstWinningSquare(game, opponent);
        if (block != null) return block;

        // Rule 3: Center — (2,2) if it is empty.
        Coordinate center = new Coordinate(2, 2);
        if (game.colorAt(center) == Color.EMPTY) return center;

        // Rule 4: Corner — first empty corner in order (1,1), (1,3), (3,1), (3,3).
        for (Coordinate corner : CORNERS) {
            if (game.colorAt(corner) == Color.EMPTY) return corner;
        }

        // Rule 5: Any — first empty Square in reading order.
        for (Coordinate c : Coordinate.all()) {
            if (game.colorAt(c) == Color.EMPTY) return c;
        }

        // Should never reach here in a valid game (game is not over when chooseCoordinate is called).
        throw new IllegalStateException("PcPlayer found no empty Square, but the game is not over");
    }

    /**
     * Returns the first Coordinate in reading order that, if filled with the given Color,
     * would complete three in a line on any Line. Returns null if no such Coordinate exists.
     *
     * "First in reading order" means we scan all 9 Coordinates in reading order and,
     * for each empty one, check whether filling it with the given Color would complete a line.
     */
    private Coordinate firstWinningSquare(TicTacToe game, Color color) {
        for (Coordinate candidate : Coordinate.all()) {
            if (game.colorAt(candidate) != Color.EMPTY) continue;
            if (wouldCompleteALine(game, candidate, color)) return candidate;
        }
        return null;
    }

    /**
     * Returns true if placing the given Color on the given (empty) Coordinate
     * would complete three in a line on any Line through that Coordinate.
     *
     * A line is completed when the other two squares already hold the given Color.
     */
    private boolean wouldCompleteALine(TicTacToe game, Coordinate candidate, Color color) {
        for (Line line : Line.all()) {
            if (!line.coordinates().contains(candidate)) continue;
            int colorCount = 0;
            for (Coordinate c : line.coordinates()) {
                if (!c.equals(candidate) && game.colorAt(c) == color) {
                    colorCount++;
                }
            }
            if (colorCount == 2) return true;
        }
        return false;
    }
}
