package tictactoe.domain;

/**
 * A domain interface: supplies the next Coordinate for a Human Player.
 * Declared in tictactoe.domain; implemented by tictactoe.console (D17).
 */
public interface CoordinateSupplier {

    /**
     * Returns the next Coordinate chosen by a person.
     * The console reads it from standard input.
     *
     * @param game the current game (read-only)
     * @return the Coordinate for the next Put; never null
     */
    Coordinate get(TicTacToe game);
}
