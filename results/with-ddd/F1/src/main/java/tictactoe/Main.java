package tictactoe;

import tictactoe.console.ConsoleGame;
import tictactoe.domain.Color;
import tictactoe.domain.Player;
import tictactoe.domain.TicTacToe;

import java.util.Scanner;

/**
 * Entry point: wires the domain and the console adapter together.
 * Creates the Players and the TicTacToe, and hands them to ConsoleGame.
 */
public final class Main {

    public static void main(String[] args) {
        Player playerX = new Player(Color.X);
        Player playerO = new Player(Color.O);
        TicTacToe game = new TicTacToe(playerX, playerO);
        String boardLogPath = System.getenv("TICTACTOE_BOARD_LOG");
        ConsoleGame consoleGame = new ConsoleGame(game, new Scanner(System.in), System.out, boardLogPath);
        consoleGame.run();
    }
}
