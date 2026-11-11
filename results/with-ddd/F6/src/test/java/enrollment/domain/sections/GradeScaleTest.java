package enrollment.domain.sections;

import enrollment.domain.RuleViolation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for GradeScale policy (I17, R28, CR-4). */
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
        // CR-4: max coursework 70, max final exam 30 → grade 100
        Grades g = scale.grades(70, 30);
        assertEquals(100, g.grade());
    }

    @Test
    void I17_coursework_above_70_rejected() {
        // CR-4: coursework 71 is out of range
        assertThrows(RuleViolation.class, () -> scale.grades(71, 0));
    }

    @Test
    void I17_coursework_72_rejected() {
        // CR-4 example: coursework 72 is rejected
        assertThrows(RuleViolation.class, () -> scale.grades(72, 0));
    }

    @Test
    void I17_finalExam_above_30_rejected() {
        // CR-4: final exam 31 is out of range
        assertThrows(RuleViolation.class, () -> scale.grades(0, 31));
    }

    @Test
    void I17_finalExam_28_is_valid() {
        // CR-4 example: final exam 28 is now valid
        Grades g = scale.grades(0, 28);
        assertEquals(28, g.grade());
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
    void R28_passing_grade_is_65() {
        // CR-4: passing grade is now 65
        assertEquals(65, scale.passingGrade());
    }

    @Test
    void R28_grade_64_fails() {
        // CR-4 example: 50 + 14 = 64, failed
        Grades g = scale.grades(50, 14);
        assertFalse(scale.passed(g));
    }

    @Test
    void R28_grade_65_passes() {
        // CR-4 example: 60 + 10 = 70, passed; also test boundary 65
        Grades g = scale.grades(60, 5);
        assertTrue(scale.passed(g));
    }

    @Test
    void R28_example_60_10_is_70_passed() {
        // CR-4 example: 60 + 10 = 70, passed
        Grades g = scale.grades(60, 10);
        assertEquals(70, g.grade());
        assertTrue(scale.passed(g));
    }

    @Test
    void R28_example_30_25_is_55_failed() {
        // CR-4 example: 30 + 25 = 55, failed
        Grades g = scale.grades(30, 25);
        assertEquals(55, g.grade());
        assertFalse(scale.passed(g));
    }

    @Test
    void R28_example_50_14_is_64_failed() {
        // CR-4 example: 50 + 14 = 64, failed
        Grades g = scale.grades(50, 14);
        assertEquals(64, g.grade());
        assertFalse(scale.passed(g));
    }
}
