package enrollment.domain.sections;

import enrollment.domain.RuleViolation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for Period parsing and invariants (I14, R24). */
class PeriodTest {

    @Test
    void I14_valid_period_parses() {
        Period p = Period.parse("7:10am-8:00am");
        assertEquals("7:10am", p.start().toString());
        assertEquals("8:00am", p.end().toString());
        assertEquals("7:10am-8:00am", p.toString());
    }

    @Test
    void I14_start_must_be_before_end() {
        assertThrows(RuleViolation.class, () -> Period.parse("8:00am-7:10am"));
    }

    @Test
    void I14_equal_times_rejected() {
        assertThrows(RuleViolation.class, () -> Period.parse("8:00am-8:00am"));
    }

    @Test
    void I14_case_insensitive_stored_in_lower_case() {
        Period p = Period.parse("7:10AM-8:00PM");
        assertEquals("7:10am-8:00pm", p.toString());
    }

    @Test
    void I14_pm_to_pm() {
        Period p = Period.parse("12:00pm-1:00pm");
        assertEquals("12:00pm-1:00pm", p.toString());
    }
}
