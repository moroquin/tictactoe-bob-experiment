package tictactoe;

import tictactoe.console.ConsoleGame;
import tictactoe.domain.Color;
import tictactoe.domain.HumanPlayer;
import tictactoe.domain.PcPlayer;
import tictactoe.domain.Player;
import tictactoe.domain.TicTacToe;

import java.util.Scanner;

/** Entry point. Creates the Players and TicTacToe, then hands them to the console adapter. */
public final class Main {

    public static void main(String[] args) {
        boolean vsPC = args.length == 1 && args[0].equals("--vs-pc");

        Player playerX = new HumanPlayer(Color.X);   // X is always the Human (I4: X starts)
        Player playerO = vsPC ? new PcPlayer(Color.O) : new HumanPlayer(Color.O);

        TicTacToe game = new TicTacToe(playerX, playerO);
        ConsoleGame console = new ConsoleGame(game, new Scanner(System.in));
        console.run();
    }
}
