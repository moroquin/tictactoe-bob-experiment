package tictactoe.domain;

/**
 * A Human Player. The console reads the Coordinate from standard input on the person's behalf.
 * chooseCoordinate() always returns null to signal that the console should read the input.
 */
public final class HumanPlayer extends Player {

    public HumanPlayer(Color color) {
        super(color);
    }

    /**
     * Returns null: the console reads the Coordinate for a Human Player from standard input.
     */
    @Override
    public Coordinate chooseCoordinate(TicTacToe game) {
        return null;
    }
}
