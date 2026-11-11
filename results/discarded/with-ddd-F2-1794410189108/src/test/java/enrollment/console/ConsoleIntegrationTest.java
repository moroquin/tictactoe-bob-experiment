package enrollment.console;

import enrollment.application.*;
import enrollment.domain.access.*;
import enrollment.domain.sections.*;
import enrollment.infrastructure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end console tests from REQUIREMENTS.md section 5 example (R1–R43).
 */
class ConsoleIntegrationTest {

    private ConsoleLoop console;
    private ByteArrayOutputStream outBytes;
    private PrintStream out;

    @BeforeEach
    void setup() {
        var userRepo      = new InMemoryUserRepository();
        var buildingRepo  = new InMemoryBuildingRepository();
        var courseRepo    = new InMemoryCourseRepository();
        var studentRepo   = new InMemoryStudentRepository();
        var professorRepo = new InMemoryProfessorRepository();
        var sectionRepo   = new InMemorySectionRepository();

        var session     = new Session();
        var permissions = new Permissions();
        var gradeScale  = new GradeScale();
        var choicePolicy= new SectionChoicePolicy();

        userRepo.add(new User(1L, "admin", "admin", UserType.ADMIN, null));
        var accessService  = new AccessService(session, userRepo, studentRepo, permissions);
        var campusService  = new CampusService(accessService, buildingRepo, sectionRepo);
        var catalogService = new CatalogService(accessService, courseRepo, sectionRepo);
        var peopleService  = new PeopleService(accessService, studentRepo, professorRepo, userRepo, sectionRepo);
        var sectionService = new SectionService(accessService, campusService, catalogService, peopleService, sectionRepo);
        var enrollmentService = new EnrollmentService(accessService, campusService, catalogService, peopleService, sectionRepo, choicePolicy, gradeScale);

        outBytes = new ByteArrayOutputStream();
        out = new PrintStream(outBytes, true, StandardCharsets.UTF_8);
        var parser    = new InstructionParser();
        var formatter = new ResponseFormatter(sectionService);
        var dispatcher= new Dispatcher(accessService, campusService, catalogService, peopleService, sectionService, enrollmentService, formatter);
        console = new ConsoleLoop(parser, dispatcher, out);
    }

    private String run(String... lines) throws IOException {
        String input = String.join("\n", lines) + "\n";
        var reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        console.run(reader);
        return outBytes.toString(StandardCharsets.UTF_8).strip();
    }

    private String runLine(String line) throws IOException {
        var reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream((line + "\n").getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        console.run(reader);
        return outBytes.toString(StandardCharsets.UTF_8).strip();
    }

    // ── R6: no session ────────────────────────────────────────────────────────

    @Test
    void R6_no_session_gives_log_in_first() throws IOException {
        String resp = runLine("QueryCourse(777);");
        assertEquals("Error: log in first", resp);
    }

    @Test
    void R6_login_without_session_works() throws IOException {
        String resp = runLine("Login(admin, admin);");
        assertTrue(resp.startsWith("OK:"));
    }

    @Test
    void R6_exit_without_session_works_and_exits() throws IOException {
        // Exit always works (R43)
        String resp = run("Exit();");
        assertEquals("", resp); // no output for Exit
    }

    // ── R7: permission denied ─────────────────────────────────────────────────

    @Test
    void R7_student_query_course_gives_permission_denied() throws IOException {
        run("Login(admin, admin);",
            "Student(200, Alice, Addr);",
            "User(alice, pw, student, 200);",
            "Logout();",
            "Login(alice, pw);");
        outBytes.reset();
        String resp = runLine("QueryCourse(777);");
        assertEquals("Error: permission denied", resp);
    }

    // ── R40: invalid instruction ──────────────────────────────────────────────

    @Test
    void R40_unknown_instruction_gives_invalid_instruction() throws IOException {
        run("Login(admin, admin);");
        outBytes.reset();
        String resp = runLine("DoSomething(1);");
        assertEquals("Error: invalid instruction", resp);
    }

    @Test
    void R40_missing_semicolon_gives_invalid_instruction() throws IOException {
        String resp = runLine("Login(admin, admin)");
        assertEquals("Error: invalid instruction", resp);
    }

    @Test
    void R40_wrong_arity_gives_invalid_instruction() throws IOException {
        run("Login(admin, admin);");
        outBytes.reset();
        String resp = runLine("Building(a, b);");
        assertEquals("Error: invalid instruction", resp);
    }

    // ── R37: blank lines ignored ──────────────────────────────────────────────

    @Test
    void R37_blank_lines_produce_no_output() throws IOException {
        String resp = run("", "   ", "");
        assertEquals("", resp);
    }

    // ── R33: instruction name is case-insensitive ─────────────────────────────

    @Test
    void R33_instruction_name_case_insensitive() throws IOException {
        String resp = runLine("login(admin, admin);");
        assertTrue(resp.startsWith("OK:"));
    }

    // ── Example from section 5 ────────────────────────────────────────────────

    @Test
    void example_section5_enroll_places_students_in_section1() throws IOException {
        run("Login(admin, admin);",
            "Student(200145785, \"Fernando Marroquín\", \"Quetzaltenango\");",
            "Student(200745781, \"Alfonso Antonio\", \"Cantel\");",
            "Student(200478542, \"Mario Morales\", \"La Esperanza\");",
            "User(Secretary, murphy1, staff);",
            "User(Control, control123, staff);",
            "User(FMarroquín, fffmmm, student, 200145785);",
            "Professor(12345, Prof1, City);",
            "Professor(47854, Prof2, City);",
            "Building(G);",
            "Building(D);",
            "Classroom(G, 12, 75);",
            "Classroom(G, 105, 60);",
            "Classroom(G, 109, 40);",
            "Classroom(G, 110, 35);",
            "Course(777, compilers1, 5, 4);",
            "Course(771, programming2, 4, 4);",
            "Section(1, 7:10am-8:00am, monday, 777, 105, G, 12345);",
            "Section(2, 8:00am-8:50am, monday, 777, 105, G, 12345);",
            "Section(5, 7:10am-8:00am, tuesday, 771, 109, G, 47854);",
            "Enroll(200145785, 777, 30, 25);",
            "Enroll(200745781, 777, 60, 10);",
            "Enroll(200478542, 771, 70, 20);");

        // The next enroll in course 777 should go to section 1
        outBytes.reset();
        String resp = runLine("Enroll(200478542, 777, 0, 0);");
        assertEquals("OK: 200478542 enrolled in course 777 in section 1", resp);
    }

    @Test
    void example_section5_delete_course_with_sections_fails() throws IOException {
        run("Login(admin, admin);",
            "Building(G);",
            "Classroom(G, 105, 60);",
            "Professor(12345, Prof1, City);",
            "Course(777, compilers1, 5, 4);",
            "Section(1, 7:10am-8:00am, monday, 777, 105, G, 12345);");
        outBytes.reset();
        String resp = runLine("DeleteCourse(777);");
        assertTrue(resp.startsWith("Error:"), "Expected Error but got: " + resp);
    }

    @Test
    void example_section5_student_enrollMe() throws IOException {
        run("Login(admin, admin);",
            "Student(200145785, Fernando, Quetzaltenango);",
            "User(FMarroquín, fffmmm, student, 200145785);",
            "Professor(47854, Prof2, City);",
            "Building(G);",
            "Classroom(G, 109, 40);",
            "Course(771, programming2, 4, 4);",
            "Section(5, 7:10am-8:00am, tuesday, 771, 109, G, 47854);",
            "Logout();",
            "Login(FMarroquín, fffmmm);");
        outBytes.reset();
        String resp = runLine("EnrollMe(771);");
        assertEquals("OK: 200145785 enrolled in course 771 in section 5", resp);
    }

    // ── Query formats (section 4.3) ───────────────────────────────────────────

    @Test
    void query_user_format_admin() throws IOException {
        run("Login(admin, admin);");
        outBytes.reset();
        String resp = runLine("QueryUser(1);");
        assertEquals("1 | admin | admin", resp);
    }

    @Test
    void query_building_no_classrooms() throws IOException {
        run("Login(admin, admin);", "Building(D);");
        outBytes.reset();
        String resp = runLine("QueryBuilding(D);");
        assertEquals("D | no classrooms", resp);
    }

    @Test
    void query_building_with_classrooms() throws IOException {
        run("Login(admin, admin);",
            "Building(G);",
            "Classroom(G, 12, 75);",
            "Classroom(G, 105, 60);",
            "Classroom(G, 109, 40);",
            "Classroom(G, 110, 35);");
        outBytes.reset();
        String resp = runLine("QueryBuilding(G);");
        assertEquals("G | 12 (75), 105 (60), 109 (40), 110 (35)", resp);
    }

    @Test
    void query_course_format() throws IOException {
        run("Login(admin, admin);", "Course(777, compilers1, 5, 4);");
        outBytes.reset();
        String resp = runLine("QueryCourse(777);");
        assertEquals("777 | compilers1 | 5 | 4", resp);
    }

    @Test
    void query_section_format() throws IOException {
        run("Login(admin, admin);",
            "Building(G);",
            "Classroom(G, 105, 60);",
            "Professor(12345, Prof1, City);",
            "Course(777, compilers1, 5, 4);",
            "Section(1, 7:10am-8:00am, monday, 777, 105, G, 12345);",
            "Student(200145785, Fernando, Q);",
            "Student(200745781, Alfonso, C);",
            "Enroll(200145785, 777, 30, 25);",
            "Enroll(200745781, 777, 60, 10);");
        outBytes.reset();
        String resp = runLine("QuerySection(1);");
        assertEquals("1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/60", resp);
    }
    // ── CR-1: ModifySection and DeleteSection ─────────────────────────────────

    @Test
    void I22_deleteSection_noEnrollments_succeeds() throws IOException {
        run("Login(admin, admin);",
            "Building(G);",
            "Classroom(G, 105, 60);",
            "Professor(12345, Prof1, City);",
            "Course(777, compilers1, 5, 4);",
            "Section(1, 7:10am-8:00am, monday, 777, 105, G, 12345);");
        outBytes.reset();
        String resp = runLine("DeleteSection(1);");
        assertTrue(resp.startsWith("OK:"), "Expected OK but got: " + resp);
    }

    @Test
    void I22_deleteSection_withEnrollments_rejected() throws IOException {
        run("Login(admin, admin);",
            "Building(G);",
            "Classroom(G, 105, 60);",
            "Professor(12345, Prof1, City);",
            "Course(777, compilers1, 5, 4);",
            "Section(1, 7:10am-8:00am, monday, 777, 105, G, 12345);",
            "Student(200145785, Fernando, Q);",
            "Enroll(200145785, 777, 0, 0);");
        outBytes.reset();
        String resp = runLine("DeleteSection(1);");
        assertTrue(resp.startsWith("Error:"), "Expected Error but got: " + resp);
    }

    @Test
    void I2_deleteSection_freesCourse_canBeDeleted() throws IOException {
        run("Login(admin, admin);",
            "Building(G);",
            "Classroom(G, 105, 60);",
            "Professor(12345, Prof1, City);",
            "Course(777, compilers1, 5, 4);",
            "Section(1, 7:10am-8:00am, monday, 777, 105, G, 12345);",
            "DeleteSection(1);");
        outBytes.reset();
        String resp = runLine("DeleteCourse(777);");
        assertTrue(resp.startsWith("OK:"), "Expected OK but got: " + resp);
    }

    @Test
    void I23_modifySection_noEnrollments_allFieldsChange() throws IOException {
        run("Login(admin, admin);",
            "Building(G);",
            "Classroom(G, 105, 60);",
            "Building(H);",
            "Classroom(H, 201, 40);",
            "Professor(12345, Prof1, City);",
            "Professor(47854, Prof2, Town);",
            "Course(777, compilers1, 5, 4);",
            "Course(771, programming2, 4, 4);",
            "Section(1, 7:10am-8:00am, monday, 777, 105, G, 12345);");
        outBytes.reset();
        String modResp = runLine("ModifySection(1, 9:00am-10:00am, friday, 771, 201, H, 47854);");
        assertTrue(modResp.startsWith("OK:"), "Expected OK but got: " + modResp);
        outBytes.reset();
        String resp = runLine("QuerySection(1);");
        assertEquals("1 | 9:00am-10:00am | friday | 771 | 201 | H | 47854 | 0/40", resp);
    }

    @Test
    void I23_modifySection_withEnrollments_courseChange_rejected() throws IOException {
        run("Login(admin, admin);",
            "Building(G);",
            "Classroom(G, 105, 60);",
            "Professor(12345, Prof1, City);",
            "Course(777, compilers1, 5, 4);",
            "Course(771, programming2, 4, 4);",
            "Section(1, 7:10am-8:00am, monday, 777, 105, G, 12345);",
            "Student(200145785, Fernando, Q);",
            "Enroll(200145785, 777, 0, 0);");
        outBytes.reset();
        String resp = runLine("ModifySection(1, 9:00am-10:00am, friday, 771, 105, G, 12345);");
        assertTrue(resp.startsWith("Error:"), "Expected Error but got: " + resp);
    }
}
