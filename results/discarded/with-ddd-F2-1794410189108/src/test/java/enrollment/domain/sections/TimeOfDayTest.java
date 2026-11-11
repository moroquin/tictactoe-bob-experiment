package enrollment.domain.sections;

import enrollment.domain.RuleViolation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for TimeOfDay parsing and formatting (I14, R24). */
class TimeOfDayTest {

    @Test
    void I14_midnight_is_12am() {
        TimeOfDay t = TimeOfDay.parse("12:00am");
        assertEquals(0, t.minutesFromMidnight());
        assertEquals("12:00am", t.toString());
    }

    @Test
    void I14_noon_is_12pm() {
        TimeOfDay t = TimeOfDay.parse("12:00pm");
        assertEquals(720, t.minutesFromMidnight());
        assertEquals("12:00pm", t.toString());
    }

    @Test
    void I14_7_10am_parses_correctly() {
        TimeOfDay t = TimeOfDay.parse("7:10am");
        assertEquals(7 * 60 + 10, t.minutesFromMidnight());
        assertEquals("7:10am", t.toString());
    }

    @Test
    void I14_1pm_is_13h() {
        TimeOfDay t = TimeOfDay.parse("1:00pm");
        assertEquals(13 * 60, t.minutesFromMidnight());
        assertEquals("1:00pm", t.toString());
    }

    @Test
    void I14_case_insensitive_AM() {
        TimeOfDay t = TimeOfDay.parse("8:00AM");
        assertEquals("8:00am", t.toString());
    }

    @Test
    void I14_hour_0_is_invalid() {
        assertThrows(RuleViolation.class, () -> TimeOfDay.parse("0:00am"));
    }

    @Test
    void I14_hour_13_is_invalid() {
        assertThrows(RuleViolation.class, () -> TimeOfDay.parse("13:00am"));
    }

    @Test
    void I14_minutes_60_is_invalid() {
        assertThrows(RuleViolation.class, () -> TimeOfDay.parse("1:60am"));
    }

    @Test
    void I14_no_ampm_is_invalid() {
        assertThrows(RuleViolation.class, () -> TimeOfDay.parse("8:00"));
    }

    @Test
    void I14_11_59pm_is_valid() {
        TimeOfDay t = TimeOfDay.parse("11:59pm");
        assertEquals("11:59pm", t.toString());
    }
}
