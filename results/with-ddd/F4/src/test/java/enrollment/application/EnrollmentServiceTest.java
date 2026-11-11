package enrollment.application;

import enrollment.domain.RuleViolation;
import enrollment.domain.access.*;
import enrollment.domain.people.StudentNumber;
import enrollment.domain.sections.*;
import enrollment.infrastructure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the enrollment use cases (I18, I19, I20, R29, R30, R31).
 */
class EnrollmentServiceTest {

    private AccessService accessService;
    private CampusService campusService;
    private CatalogService catalogService;
    private PeopleService peopleService;
    private SectionService sectionService;
    private EnrollmentService enrollmentService;

    // Shared repos for the I20 test
    private InMemoryUserRepository userRepo;
    private InMemorySectionRepository sectionRepo;

    @BeforeEach
    void setup() {
        userRepo      = new InMemoryUserRepository();
        var buildingRepo  = new InMemoryBuildingRepository();
        var courseRepo    = new InMemoryCourseRepository();
        var studentRepo   = new InMemoryStudentRepository();
        var professorRepo = new InMemoryProfessorRepository();
        sectionRepo   = new InMemorySectionRepository();

        var session     = new Session();
        var permissions = new Permissions();
        var gradeScale  = new GradeScale();
        var choicePolicy= new SectionChoicePolicy();

        userRepo.add(new User(1L, "admin", "admin", UserType.ADMIN, null));
        accessService = new AccessService(session, userRepo, studentRepo, permissions);
        accessService.login("admin", "admin");

        campusService  = new CampusService(accessService, buildingRepo, sectionRepo);
        catalogService = new CatalogService(accessService, courseRepo, sectionRepo);
        peopleService  = new PeopleService(accessService, studentRepo, professorRepo,
                                           userRepo, sectionRepo);
        sectionService = new SectionService(accessService, campusService, catalogService,
                                            peopleService, sectionRepo);
        enrollmentService = new EnrollmentService(accessService, campusService, catalogService,
                                                  peopleService, sectionRepo, choicePolicy, gradeScale);

        // Setup test data
        peopleService.createStudent(100L, "Alice", "Addr");
        peopleService.createStudent(101L, "Bob", "Addr");
        peopleService.createProfessor(1L, "Prof1", "City");
        campusService.createBuilding("G");
        campusService.createClassroom("G", 101, 2); // capacity 2
        campusService.createClassroom("G", 102, 1); // capacity 1
        catalogService.createCourse(777L, "compilers1", 5, 4);
        sectionService.createSection(1L, "7:10am-8:00am", "monday", 777L, 101, "G", 1L);
        sectionService.createSection(2L, "8:00am-8:50am", "monday", 777L, 102, "G", 1L);
    }

    @Test
    void I18_tie_on_enrollments_places_in_lowest_code_section() {
        // Both sections empty → tie → lowest code (section 1)
        Section s = enrollmentService.enroll(100L, 777L, 30, 25);
        assertEquals(new SectionCode(1L), s.code());
    }

    @Test
    void I18_full_section_skipped_next_chosen() {
        // Section 1 capacity=2, section 2 capacity=1.
        // Student 100 → section 1 (both empty, tie → lowest code 1)
        // Student 101 → section 2 (section 1 has 1, section 2 has 0 → fewest = section 2)
        // Section 2 is now full (capacity 1).
        // Student 102 → only section 1 has room → section 1
        enrollmentService.enroll(100L, 777L, 0, 0);
        enrollmentService.enroll(101L, 777L, 0, 0);
        peopleService.createStudent(102L, "Carol", "Addr");
        Section s = enrollmentService.enroll(102L, 777L, 0, 0);
        assertEquals(new SectionCode(1L), s.code());
    }

    @Test
    void I19_aStudentEnrollsInACourseOnlyOnce() {
        enrollmentService.enroll(100L, 777L, 0, 0);
        assertThrows(RuleViolation.class, () -> enrollmentService.enroll(100L, 777L, 0, 0));
    }

    @Test
    void R29_no_sections_rejects_enrollment() {
        catalogService.createCourse(999L, "empty", 1, 1);
        assertThrows(RuleViolation.class, () -> enrollmentService.enroll(100L, 999L, 0, 0));
    }

    @Test
    void R29_all_sections_full_rejects_enrollment() {
        // Fill both sections: section1 cap=2, section2 cap=1
        enrollmentService.enroll(100L, 777L, 0, 0);
        enrollmentService.enroll(101L, 777L, 0, 0);
        peopleService.createStudent(102L, "Carol", "Addr");
        enrollmentService.enroll(102L, 777L, 0, 0); // goes to section 2
        peopleService.createStudent(103L, "Dave", "Addr");
        assertThrows(RuleViolation.class, () -> enrollmentService.enroll(103L, 777L, 0, 0));
    }

    @Test
    void I20_enrollMe_starts_with_zero_grades() {
        // Create a student-type user linked to student 100
        accessService.createUser("alice", "pw", UserType.STUDENT,
                Optional.of(new StudentNumber(100L)));
        accessService.logout();
        accessService.login("alice", "pw");
        Section s = enrollmentService.enrollMe(777L);
        var e = s.enrollments().stream()
                .filter(en -> en.student().value() == 100L).findFirst().orElseThrow();
        assertEquals(0, e.grades().coursework());
        assertEquals(0, e.grades().finalExam());
    }
}
