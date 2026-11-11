package enrollment.domain.sections;

import enrollment.domain.campus.BuildingId;
import enrollment.domain.campus.ClassroomRef;
import enrollment.domain.catalog.CourseCode;
import enrollment.domain.people.ProfessorId;
import enrollment.domain.people.StudentNumber;
import enrollment.domain.RuleViolation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for Section aggregate (I18, R29, R30). */
class SectionTest {

    private final GradeScale scale = new GradeScale();
    private final ClassroomRef room = new ClassroomRef(new BuildingId(1L), 101);

    private Section makeSection() {
        return new Section(new SectionCode(1L), Period.parse("7:00am-8:00am"),
                Day.MONDAY, new CourseCode(1L), room, new ProfessorId(1L));
    }

    @Test
    void I18_section_has_room_when_below_capacity() {
        Section s = makeSection();
        assertTrue(s.hasRoom(2));
    }

    @Test
    void I18_section_full_when_enrolled_equals_capacity() {
        Section s = makeSection();
        s.enroll(new StudentNumber(1L), scale.grades(0, 0), 1);
        assertFalse(s.hasRoom(1));
    }

    @Test
    void I18_enroll_beyond_capacity_throws() {
        Section s = makeSection();
        s.enroll(new StudentNumber(1L), scale.grades(0, 0), 1);
        assertThrows(RuleViolation.class, () ->
                s.enroll(new StudentNumber(2L), scale.grades(0, 0), 1));
    }

    @Test
    void R30_unenroll_removes_student() {
        Section s = makeSection();
        StudentNumber sn = new StudentNumber(1L);
        s.enroll(sn, scale.grades(0, 0), 5);
        assertEquals(1, s.enrollmentCount());
        s.unenroll(sn);
        assertEquals(0, s.enrollmentCount());
    }

    @Test
    void R32_change_grades_updates_enrollment() {
        Section s = makeSection();
        StudentNumber sn = new StudentNumber(1L);
        s.enroll(sn, scale.grades(0, 0), 5);
        s.changeGrades(sn, scale.grades(50, 20));
        Enrollment e = s.enrollments().get(0);
        assertEquals(50, e.grades().coursework());
        assertEquals(20, e.grades().finalExam());
    }

    @Test
    void R30_unenroll_unknown_student_throws() {
        Section s = makeSection();
        assertThrows(RuleViolation.class, () ->
                s.unenroll(new StudentNumber(999L)));
    }
}
