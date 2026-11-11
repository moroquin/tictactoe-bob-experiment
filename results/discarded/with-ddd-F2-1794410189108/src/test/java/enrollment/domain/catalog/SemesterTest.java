package enrollment.domain.catalog;

import enrollment.domain.RuleViolation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for Semester value object (I13). */
class SemesterTest {

    @Test
    void I13_valid_semester_1() {
        assertEquals(1, new Semester(1).value());
    }

    @Test
    void I13_valid_semester_10() {
        assertEquals(10, new Semester(10).value());
    }

    @Test
    void I13_semester_0_rejected() {
        assertThrows(RuleViolation.class, () -> new Semester(0));
    }

    @Test
    void I13_semester_11_rejected() {
        assertThrows(RuleViolation.class, () -> new Semester(11));
    }
}
