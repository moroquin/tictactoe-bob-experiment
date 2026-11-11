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

    // ── Overlap (CR-6, D30) ───────────────────────────────────────────────────

    @Test
    void CR6_overlapping_periods_return_true() {
        Period a = Period.parse("7:10am-8:00am");
        Period b = Period.parse("7:30am-8:20am");
        assertTrue(a.overlaps(b));
        assertTrue(b.overlaps(a));
    }

    @Test
    void CR6_touching_periods_do_not_overlap() {
        // 7:10am-8:00am and 8:00am-8:50am share only the endpoint 8:00am
        Period a = Period.parse("7:10am-8:00am");
        Period b = Period.parse("8:00am-8:50am");
        assertFalse(a.overlaps(b));
        assertFalse(b.overlaps(a));
    }

    @Test
    void CR6_non_overlapping_periods_return_false() {
        Period a = Period.parse("7:00am-8:00am");
        Period b = Period.parse("9:00am-10:00am");
        assertFalse(a.overlaps(b));
        assertFalse(b.overlaps(a));
    }

    @Test
    void CR6_contained_period_overlaps() {
        Period outer = Period.parse("7:00am-9:00am");
        Period inner = Period.parse("7:30am-8:30am");
        assertTrue(outer.overlaps(inner));
        assertTrue(inner.overlaps(outer));
    }

    @Test
    void CR6_same_period_overlaps_itself() {
        Period a = Period.parse("7:10am-8:00am");
        assertTrue(a.overlaps(a));
    }
}
