package enrollment.application;

import enrollment.domain.RuleViolation;
import enrollment.domain.access.*;
import enrollment.domain.sections.*;
import enrollment.infrastructure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for SectionService.modifySection and deleteSection (I22, I23, R26b, R26c). */
class SectionServiceTest {

    private SectionService sectionService;
    private CatalogService catalogService;
    private CampusService campusService;
    private PeopleService peopleService;
    private InMemorySectionRepository sectionRepo;

    @BeforeEach
    void setup() {
        var userRepo      = new InMemoryUserRepository();
        var studentRepo   = new InMemoryStudentRepository();
        var professorRepo = new InMemoryProfessorRepository();
        var buildingRepo  = new InMemoryBuildingRepository();
        var courseRepo    = new InMemoryCourseRepository();
        sectionRepo       = new InMemorySectionRepository();

        var session     = new Session();
        var permissions = new Permissions();
        var accessService = new AccessService(session, userRepo, studentRepo, permissions);
        userRepo.add(new User(1L, "admin", "admin", UserType.ADMIN, null));
        accessService.login("admin", "admin");

        campusService  = new CampusService(accessService, buildingRepo, sectionRepo);
        catalogService = new CatalogService(accessService, courseRepo, sectionRepo);
        peopleService  = new PeopleService(accessService, studentRepo, professorRepo, userRepo, sectionRepo);
        sectionService = new SectionService(accessService, campusService, catalogService, peopleService, sectionRepo);

        // Common fixtures: building G with classrooms 101 (cap 30) and 102 (cap 5)
        campusService.createBuilding("G");
        campusService.createClassroom("G", 101, 30);
        campusService.createClassroom("G", 102, 5);
        // courses 10 and 20
        catalogService.createCourse(10L, "Math", 1, 3);
        catalogService.createCourse(20L, "Physics", 2, 4);
        // professor 1
        peopleService.createProfessor(1L, "Prof1", "City");
        peopleService.createProfessor(2L, "Prof2", "City");
        // section 1 on course 10
        sectionService.createSection(1L, "7:00am-8:00am", "monday", 10L, 101, "G", 1L);
    }

    // ── ModifySection ─────────────────────────────────────────────────────────

    @Test
    void R26b_modify_section_changes_all_fields() {
        sectionService.modifySection(1L, "9:00am-10:00am", "tuesday", 20L, 102, "G", 2L);
        var s = sectionService.querySection(1L);
        assertEquals("9:00am-10:00am", s.period().toString());
        assertEquals(Day.TUESDAY, s.day());
        assertEquals(20L, s.course().value());
        assertEquals(102, s.classroom().number());
        assertEquals(2L, s.professor().value());
    }

    @Test
    void R26b_modify_section_nonexistent_throws() {
        assertThrows(RuleViolation.class,
                () -> sectionService.modifySection(99L, "9:00am-10:00am", "tuesday", 20L, 102, "G", 2L));
    }

    @Test
    void I22_modify_section_invalid_period_throws() {
        assertThrows(RuleViolation.class,
                () -> sectionService.modifySection(1L, "bad-period", "monday", 10L, 101, "G", 1L));
    }

    @Test
    void I22_modify_section_invalid_day_throws() {
        assertThrows(RuleViolation.class,
                () -> sectionService.modifySection(1L, "7:00am-8:00am", "funday", 10L, 101, "G", 1L));
    }

    @Test
    void I22_modify_section_missing_course_throws() {
        assertThrows(RuleViolation.class,
                () -> sectionService.modifySection(1L, "7:00am-8:00am", "monday", 999L, 101, "G", 1L));
    }

    @Test
    void I22_modify_section_missing_classroom_throws() {
        assertThrows(RuleViolation.class,
                () -> sectionService.modifySection(1L, "7:00am-8:00am", "monday", 10L, 999, "G", 1L));
    }

    @Test
    void I22_modify_section_missing_professor_throws() {
        assertThrows(RuleViolation.class,
                () -> sectionService.modifySection(1L, "7:00am-8:00am", "monday", 10L, 101, "G", 999L));
    }

    // ── ModifySection with enrollments ────────────────────────────────────────

    private void enrollStudent(long studentNumber, long courseCode) {
        peopleService.createStudent(studentNumber, "Student" + studentNumber, "Addr");
        var gradeScale    = new GradeScale();
        var choicePolicy  = new SectionChoicePolicy();
        var enrollService = new EnrollmentService(
                /* accessService obtained via campusService — we need accessService */
                new AccessService(
                        new Session() {{ /* hack: need an open session */ }},
                        new InMemoryUserRepository(),
                        new InMemoryStudentRepository(),
                        new Permissions()),
                campusService, catalogService, peopleService, sectionRepo, choicePolicy, gradeScale);
        // Use the sectionRepo directly to enroll without a second service
        var sections = sectionRepo.ofCourse(new enrollment.domain.catalog.CourseCode(courseCode));
        var section  = sections.get(0);
        section.enroll(new enrollment.domain.people.StudentNumber(studentNumber),
                gradeScale.grades(0, 0), 30);
    }

    @Test
    void I22_modify_with_enrollments_cannot_change_course() {
        // Enroll directly into the section aggregate
        var gradeScale = new GradeScale();
        var section = sectionService.querySection(1L);
        section.enroll(new enrollment.domain.people.StudentNumber(1001L),
                gradeScale.grades(0, 0), 30);

        // Attempt to change course — must fail
        assertThrows(RuleViolation.class,
                () -> sectionService.modifySection(1L, "7:00am-8:00am", "monday", 20L, 101, "G", 1L));
    }

    @Test
    void I22_modify_with_enrollments_can_change_other_fields() {
        var gradeScale = new GradeScale();
        var section = sectionService.querySection(1L);
        section.enroll(new enrollment.domain.people.StudentNumber(1001L),
                gradeScale.grades(0, 0), 30);

        // Changing day/period/professor/classroom (same course, classroom with enough capacity) must succeed
        assertDoesNotThrow(
                () -> sectionService.modifySection(1L, "9:00am-10:00am", "tuesday", 10L, 101, "G", 2L));
    }

    @Test
    void I22_modify_with_enrollments_new_classroom_too_small_throws() {
        // Enroll 6 students; classroom 102 has capacity 5
        var gradeScale = new GradeScale();
        var section = sectionService.querySection(1L);
        for (long i = 1001; i <= 1006; i++) {
            section.enroll(new enrollment.domain.people.StudentNumber(i), gradeScale.grades(0, 0), 30);
        }
        assertThrows(RuleViolation.class,
                () -> sectionService.modifySection(1L, "7:00am-8:00am", "monday", 10L, 102, "G", 1L));
    }

    // ── DeleteSection ─────────────────────────────────────────────────────────

    @Test
    void I23_delete_section_without_enrollments_succeeds() {
        sectionService.deleteSection(1L);
        assertThrows(RuleViolation.class, () -> sectionService.querySection(1L));
    }

    @Test
    void I23_delete_section_with_enrollments_throws() {
        var gradeScale = new GradeScale();
        var section = sectionService.querySection(1L);
        section.enroll(new enrollment.domain.people.StudentNumber(1001L),
                gradeScale.grades(0, 0), 30);
        assertThrows(RuleViolation.class, () -> sectionService.deleteSection(1L));
    }

    @Test
    void R26b_delete_nonexistent_section_throws() {
        assertThrows(RuleViolation.class, () -> sectionService.deleteSection(99L));
    }

    @Test
    void R26c_after_delete_course_can_be_deleted() {
        // Section 1 used course 10; deleting it should allow course 10 to be deleted
        sectionService.deleteSection(1L);
        assertDoesNotThrow(() -> catalogService.deleteCourse(10L));
    }
}
