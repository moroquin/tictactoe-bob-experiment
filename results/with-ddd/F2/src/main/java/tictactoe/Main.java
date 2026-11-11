package tictactoe;

import tictactoe.console.ConsoleGame;
import tictactoe.domain.Color;
import tictactoe.domain.HumanPlayer;
import tictactoe.domain.PcPlayer;
import tictactoe.domain.Player;
import tictactoe.domain.TicTacToe;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Entry point: wires the domain and the console adapter together.
 * Creates the Players and the TicTacToe, and hands them to ConsoleGame.
 *
 * Without arguments: two Human Players (exactly as before).
 * With --vs-pc: Human Player plays X; PC Player plays O (D19).
 */
public final class Main {

    public static void main(String[] args) {
        boolean vspc = Arrays.asList(args).contains("--vs-pc");

        Scanner scanner = new Scanner(System.in);
        String boardLogPath = System.getenv("TICTACTOE_BOARD_LOG");

        // ConsoleGame implements CoordinateSupplier; we create it before the players so
        // HumanPlayer can hold a reference to it. The game is wired afterwards.
        // To break the circular dependency we pass a lambda that is captured by the ConsoleGame
        // instance created below: create the scanner-backed supplier via ConsoleGame itself.
        // Simplest approach: create ConsoleGame first (with a placeholder game), then reassign.
        // Instead, use a one-element array trick to share the reference.
        //
        // Actually: ConsoleGame takes the TicTacToe in its constructor, so we need the players
        // to build TicTacToe first. HumanPlayer needs a CoordinateSupplier. We can use a
        // forwarding supplier that delegates to the ConsoleGame once it is created.

        // Use a ForwardingSupplier to break the initialization cycle.
        ForwardingSupplier supplierX = new ForwardingSupplier();
        ForwardingSupplier supplierO = vspc ? null : new ForwardingSupplier();

        Player playerX = new HumanPlayer(Color.X, supplierX);
        Player playerO = vspc
                ? new PcPlayer(Color.O)
                : new HumanPlayer(Color.O, supplierO);

        TicTacToe game = new TicTacToe(playerX, playerO);
        ConsoleGame consoleGame = new ConsoleGame(game, scanner, System.out, boardLogPath);

        // Wire the forwarding suppliers to the actual ConsoleGame.
        supplierX.delegate = consoleGame;
        if (supplierO != null) supplierO.delegate = consoleGame;

        consoleGame.run();
    }

    /**
     * A CoordinateSupplier that forwards to another supplier set after construction.
     * Used to break the initialization cycle between HumanPlayer and ConsoleGame.
     */
    private static final class ForwardingSupplier implements tictactoe.domain.CoordinateSupplier {
        tictactoe.domain.CoordinateSupplier delegate;

        @Override
        public tictactoe.domain.Coordinate get(TicTacToe game) {
            return delegate.get(game);
        }
    }
}
