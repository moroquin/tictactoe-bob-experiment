package tictactoe.domain;

/**
 * A Player controlled by a person at the console.
 * The console reads a Coordinate from the input on their behalf
 * via a CoordinateSupplier (D17).
 */
public final class HumanPlayer extends Player {

    private final CoordinateSupplier supplier;

    /**
     * Creates a HumanPlayer.
     *
     * @param color    the Color of this player (X or O)
     * @param supplier supplies Coordinates from console input
     */
    public HumanPlayer(Color color, CoordinateSupplier supplier) {
        super(color);
        this.supplier = supplier;
    }

    /**
     * Delegates to the CoordinateSupplier (reads from console input).
     */
    @Override
    public Coordinate chooseCoordinate(TicTacToe game) {
        return supplier.get(game);
    }
}
