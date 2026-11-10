package tictactoe;

import tictactoe.console.ConsoleGame;
import tictactoe.domain.Color;
import tictactoe.domain.Player;
import tictactoe.domain.TicTacToe;

import java.util.Scanner;

/** Entry point. Creates the Players and TicTacToe, then hands them to the console adapter. */
public final class Main {

    public static void main(String[] args) {
        Player playerX = new Player(Color.X);
        Player playerO = new Player(Color.O);
        TicTacToe game = new TicTacToe(playerX, playerO);
        ConsoleGame console = new ConsoleGame(game, new Scanner(System.in));
        console.run();
    }
}
