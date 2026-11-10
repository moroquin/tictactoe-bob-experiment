package tictactoe.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoundTest {

    private final Player playerX = new Player(Color.X);
    private final Player playerO = new Player(Color.O);

    @Test
    void I4_xAlwaysStartsFirst() {
        Round round = new Round(playerX, playerO);
        assertEquals(playerX, round.current());
    }

    @Test
    void I4_afterNextCurrentIsO() {
        Round round = new Round(playerX, playerO);
        round.next();
        assertEquals(playerO, round.current());
    }

    @Test
    void I4_roundAlternatesXOX() {
        Round round = new Round(playerX, playerO);
        assertEquals(playerX, round.current());
        round.next();
        assertEquals(playerO, round.current());
        round.next();
        assertEquals(playerX, round.current());
        round.next();
        assertEquals(playerO, round.current());
    }
}
