package tictactoe.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoundTest {

    private final Player playerX = new HumanPlayer(Color.X, g -> null);
    private final Player playerO = new HumanPlayer(Color.O, g -> null);

    @Test
    void I4_xStartsFirst() {
        Round round = new Round(playerX, playerO);
        assertEquals(playerX, round.current());
    }

    @Test
    void I4_nextAlternatesFromXToO() {
        Round round = new Round(playerX, playerO);
        round.next();
        assertEquals(playerO, round.current());
    }

    @Test
    void I4_nextAlternatesFromOBackToX() {
        Round round = new Round(playerX, playerO);
        round.next(); // now O
        round.next(); // back to X
        assertEquals(playerX, round.current());
    }

    @Test
    void I4_alternatesMultipleTimes() {
        Round round = new Round(playerX, playerO);
        Player[] expected = {playerO, playerX, playerO, playerX};
        for (Player e : expected) {
            round.next();
            assertEquals(e, round.current());
        }
    }
}
