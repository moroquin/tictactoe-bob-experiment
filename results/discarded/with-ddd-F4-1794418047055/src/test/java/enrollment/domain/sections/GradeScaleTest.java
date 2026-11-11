package enrollment.domain.sections;

import enrollment.domain.RuleViolation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for GradeScale policy (I17, R28). */
class GradeScaleTest {

    private final GradeScale scale = new GradeScale();

    @Test
    void I17_valid_grades_build_correctly() {
        Grades g = scale.grades(30, 25);
        assertEquals(30, g.coursework());
        assertEquals(25, g.finalExam());
        assertEquals(55, g.grade());
    }

    @Test
    void I17_zero_zero_is_valid() {
        Grades g = scale.grades(0, 0);
        assertEquals(0, g.grade());
    }

    @Test
    void I17_max_grades() {
        Grades g = scale.grades(75, 25);
        assertEquals(100, g.grade());
    }

    @Test
    void I17_coursework_above_75_rejected() {
        assertThrows(RuleViolation.class, () -> scale.grades(76, 0));
    }

    @Test
    void I17_finalExam_above_25_rejected() {
        assertThrows(RuleViolation.class, () -> scale.grades(0, 26));
    }

    @Test
    void I17_negative_coursework_rejected() {
        assertThrows(RuleViolation.class, () -> scale.grades(-1, 0));
    }

    @Test
    void I17_negative_finalExam_rejected() {
        assertThrows(RuleViolation.class, () -> scale.grades(0, -1));
    }

    @Test
    void R28_passing_grade_is_61() {
        assertEquals(61, scale.passingGrade());
    }

    @Test
    void R28_grade_60_fails() {
        Grades g = scale.grades(60, 0);
        assertFalse(scale.passed(g));
    }

    @Test
    void R28_grade_61_passes() {
        Grades g = scale.grades(61, 0);
        assertTrue(scale.passed(g));
    }
}
