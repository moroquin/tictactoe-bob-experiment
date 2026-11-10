package tictactoe;

import tictactoe.console.ConsoleGame;
import tictactoe.console.ThreePiecesConsoleGame;
import tictactoe.domain.Color;
import tictactoe.domain.HumanPlayer;
import tictactoe.domain.PcPlayer;
import tictactoe.domain.Player;
import tictactoe.domain.ThreePiecesTicTacToe;
import tictactoe.domain.TicTacToe;

import java.util.Scanner;

/** Entry point. Creates the Players and the game, then hands them to the console adapter. */
public final class Main {

    public static void main(String[] args) {
        boolean vsPC        = args.length == 1 && args[0].equals("--vs-pc");
        boolean threePieces = args.length == 1 && args[0].equals("--three-pieces");

        Scanner scanner = new Scanner(System.in);

        if (threePieces) {
            // Three-pieces variant: two Human Players (combining with --vs-pc is out of scope)
            Player playerX = new HumanPlayer(Color.X);
            Player playerO = new HumanPlayer(Color.O);
            ThreePiecesTicTacToe game = new ThreePiecesTicTacToe(playerX, playerO);
            new ThreePiecesConsoleGame(game, scanner).run();
        } else {
            Player playerX = new HumanPlayer(Color.X);   // X is always the Human (I4: X starts)
            Player playerO = vsPC ? new PcPlayer(Color.O) : new HumanPlayer(Color.O);
            TicTacToe game = new TicTacToe(playerX, playerO);
            new ConsoleGame(game, scanner).run();
        }
    }
}
