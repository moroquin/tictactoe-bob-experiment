package enrollment.domain.sections;

import enrollment.domain.RuleViolation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for Day parsing (I15, R25). */
class DayTest {

    @Test
    void I15_all_days_parse() {
        assertEquals(Day.MONDAY,    Day.parse("monday"));
        assertEquals(Day.TUESDAY,   Day.parse("tuesday"));
        assertEquals(Day.WEDNESDAY, Day.parse("wednesday"));
        assertEquals(Day.THURSDAY,  Day.parse("thursday"));
        assertEquals(Day.FRIDAY,    Day.parse("friday"));
        assertEquals(Day.SATURDAY,  Day.parse("saturday"));
        assertEquals(Day.SUNDAY,    Day.parse("sunday"));
    }

    @Test
    void I15_case_insensitive() {
        assertEquals(Day.MONDAY, Day.parse("MONDAY"));
        assertEquals(Day.FRIDAY, Day.parse("Friday"));
    }

    @Test
    void I15_invalid_day_rejected() {
        assertThrows(RuleViolation.class, () -> Day.parse("funday"));
    }

    @Test
    void I15_display_is_lowercase() {
        assertEquals("monday", Day.MONDAY.display());
        assertEquals("sunday", Day.SUNDAY.display());
    }
}
