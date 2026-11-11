package enrollment.console;

import enrollment.application.*;
import enrollment.console.*;
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
        var reportService  = new ReportService(accessService, campusService, catalogService, peopleService, sectionRepo, gradeScale);

        outBytes = new ByteArrayOutputStream();
        out = new PrintStream(outBytes, true, StandardCharsets.UTF_8);
        var parser    = new InstructionParser();
        var formatter = new ResponseFormatter(sectionService);
        var dispatcher= new Dispatcher(accessService, campusService, catalogService, peopleService, sectionService, enrollmentService, reportService, formatter);
        var fileLoader = new FileLoader(accessService, parser, dispatcher, out);
        dispatcher.setFileLoader(fileLoader);
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

    // ── ModifySection (CR-1) ──────────────────────────────────────────────────

    @Test
    void R26b_modify_section_changes_fields_and_query_reflects_it() throws IOException {
        run("Login(admin, admin);",
            "Building(G);",
            "Classroom(G, 105, 60);",
            "Classroom(G, 109, 40);",
            "Professor(12345, Prof1, City);",
            "Professor(47854, Prof2, City);",
            "Course(777, compilers1, 5, 4);",
            "Course(771, prog2, 4, 4);",
            "Section(1, 7:10am-8:00am, monday, 777, 105, G, 12345);");
        outBytes.reset();
        String mod = runLine("ModifySection(1, 9:00am-10:00am, tuesday, 771, 109, G, 47854);");
        assertTrue(mod.startsWith("OK:"), "Expected OK but got: " + mod);
        outBytes.reset();
        String query = runLine("QuerySection(1);");
        assertEquals("1 | 9:00am-10:00am | tuesday | 771 | 109 | G | 47854 | 0/40", query);
    }

    @Test
    void I22_modify_section_with_enrollments_cannot_change_course() throws IOException {
        run("Login(admin, admin);",
            "Building(G);",
            "Classroom(G, 105, 60);",
            "Professor(12345, Prof1, City);",
            "Course(777, compilers1, 5, 4);",
            "Course(771, prog2, 4, 4);",
            "Student(100, Alice, Addr);",
            "Section(1, 7:10am-8:00am, monday, 777, 105, G, 12345);",
            "Enroll(100, 777, 0, 0);");
        outBytes.reset();
        String resp = runLine("ModifySection(1, 8:00am-9:00am, monday, 771, 105, G, 12345);");
        assertTrue(resp.startsWith("Error:"), "Expected Error but got: " + resp);
    }

    @Test
    void I22_modify_section_with_enrollments_new_classroom_too_small_throws() throws IOException {
        run("Login(admin, admin);",
            "Building(G);",
            "Classroom(G, 105, 60);",
            "Classroom(G, 109, 1);",
            "Professor(12345, Prof1, City);",
            "Course(777, compilers1, 5, 4);",
            "Student(100, Alice, Addr);",
            "Student(200, Bob, Addr);",
            "Section(1, 7:10am-8:00am, monday, 777, 105, G, 12345);",
            "Enroll(100, 777, 0, 0);",
            "Enroll(200, 777, 0, 0);");
        outBytes.reset();
        String resp = runLine("ModifySection(1, 8:00am-9:00am, monday, 777, 109, G, 12345);");
        assertTrue(resp.startsWith("Error:"), "Expected Error but got: " + resp);
    }

    // ── DeleteSection (CR-1) ──────────────────────────────────────────────────

    @Test
    void R26c_delete_empty_section_succeeds() throws IOException {
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
    void I23_delete_section_with_enrollments_fails() throws IOException {
        run("Login(admin, admin);",
            "Building(G);",
            "Classroom(G, 105, 60);",
            "Professor(12345, Prof1, City);",
            "Course(777, compilers1, 5, 4);",
            "Student(100, Alice, Addr);",
            "Section(1, 7:10am-8:00am, monday, 777, 105, G, 12345);",
            "Enroll(100, 777, 0, 0);");
        outBytes.reset();
        String resp = runLine("DeleteSection(1);");
        assertTrue(resp.startsWith("Error:"), "Expected Error but got: " + resp);
    }

    @Test
    void R26c_after_delete_section_course_can_be_deleted() throws IOException {
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
    void R26c_after_delete_section_professor_can_be_deleted() throws IOException {
        run("Login(admin, admin);",
            "Building(G);",
            "Classroom(G, 105, 60);",
            "Professor(12345, Prof1, City);",
            "Course(777, compilers1, 5, 4);",
            "Section(1, 7:10am-8:00am, monday, 777, 105, G, 12345);",
            "DeleteSection(1);");
        outBytes.reset();
        String resp = runLine("DeleteProfessor(12345);");
        assertTrue(resp.startsWith("OK:"), "Expected OK but got: " + resp);
    }

    @Test
    void R26c_after_delete_section_classroom_can_be_deleted() throws IOException {
        run("Login(admin, admin);",
            "Building(G);",
            "Classroom(G, 105, 60);",
            "Professor(12345, Prof1, City);",
            "Course(777, compilers1, 5, 4);",
            "Section(1, 7:10am-8:00am, monday, 777, 105, G, 12345);",
            "DeleteSection(1);");
        outBytes.reset();
        String resp = runLine("DeleteClassroom(G, 105);");
        assertTrue(resp.startsWith("OK:"), "Expected OK but got: " + resp);
    }

    // ── Reports (CR-2) ────────────────────────────────────────────────────────

    /** Load the full CR-2 example data (sections 1-12 plus enrollments). */
    private void loadCR2ExampleData() throws IOException {
        run("Login(admin, admin);",
            "Student(200145785, \"Fernando Marroquín\", \"Quetzaltenango\");",
            "Student(200745781, \"Alfonso Antonio\", \"Cantel\");",
            "Student(200478542, \"Mario Morales\", \"La Esperanza\");",
            "Professor(12345, Prof1, City);",
            "Professor(47854, Prof2, City);",
            "Professor(12445, Prof3, City);",
            "Professor(12365, Prof5, City);",
            "Building(G);",
            "Classroom(G, 105, 60);",
            "Classroom(G, 109, 40);",
            "Classroom(G, 110, 35);",
            "Course(777, compilers1, 5, 4);",
            "Course(771, programming2, 4, 4);",
            "Course(779, computerarch, 7, 4);",
            "Course(732, Statistics1, 4, 4);",
            "Course(795, FormalLangs, 4, 4);",
            "Section(1, 7:10am-8:00am, monday, 777, 105, G, 12345);",
            "Section(2, 8:00am-8:50am, monday, 777, 105, G, 12345);",
            "Section(5, 7:10am-8:00am, tuesday, 771, 109, G, 12345);",
            "Section(6, 8:00am-8:50am, tuesday, 771, 109, G, 12345);",
            "Section(7, 7:10am-8:00am, thursday, 771, 109, G, 47854);",
            "Section(8, 8:00am-8:50am, thursday, 771, 109, G, 47854);",
            "Enroll(200145785, 777, 30, 25);",
            "Enroll(200745781, 777, 60, 10);",
            "Enroll(200478542, 771, 70, 20);");
    }

    @Test
    void I25_studentCourses_returns_one_row_per_enrollment_ascending_section_code() throws IOException {
        loadCR2ExampleData();
        outBytes.reset();
        String resp = runLine("StudentCourses(200145785);");
        assertEquals("777 | compilers1 | section 1 | coursework 30 | final 25 | failed", resp);
    }

    @Test
    void I25_studentCourses_no_enrollments_returns_no_courses() throws IOException {
        run("Login(admin, admin);",
            "Student(200145785, Fernando, Q);");
        outBytes.reset();
        String resp = runLine("StudentCourses(200145785);");
        assertEquals("No courses", resp);
    }

    @Test
    void I25_studentCourses_student_not_found_returns_error() throws IOException {
        run("Login(admin, admin);");
        outBytes.reset();
        String resp = runLine("StudentCourses(9999);");
        assertTrue(resp.startsWith("Error:"), "Expected Error but got: " + resp);
    }

    @Test
    void I25_student_cannot_query_another_students_courses() throws IOException {
        run("Login(admin, admin);",
            "Student(200145785, Fernando, Q);",
            "Student(200745781, Alfonso, C);",
            "User(alfUser, pw, student, 200745781);",
            "Logout();",
            "Login(alfUser, pw);");
        outBytes.reset();
        String resp = runLine("StudentCourses(200145785);");
        assertEquals("Error: permission denied", resp);
    }

    @Test
    void I25_student_can_query_own_courses() throws IOException {
        run("Login(admin, admin);",
            "Student(200145785, Fernando, Q);",
            "User(fUser, pw, student, 200145785);",
            "Logout();",
            "Login(fUser, pw);");
        outBytes.reset();
        String resp = runLine("StudentCourses(200145785);");
        assertEquals("No courses", resp);
    }

    @Test
    void I26_courseStudents_returns_enrollments_in_section_then_enrollment_order() throws IOException {
        loadCR2ExampleData();
        // Under CR-3 balanced policy: 200145785 → section 1 (both empty, lowest code);
        // 200745781 → section 2 (section 1 has 1, section 2 has 0).
        outBytes.reset();
        String resp = runLine("CourseStudents(777);");
        String expected =
            "200145785 | Fernando Marroquín | section 1 | coursework 30 | final 25 | failed\n" +
            "200745781 | Alfonso Antonio | section 2 | coursework 60 | final 10 | passed";
        assertEquals(expected, resp);
    }

    @Test
    void I26_courseStudents_no_enrollments_returns_no_students() throws IOException {
        run("Login(admin, admin);",
            "Course(777, compilers1, 5, 4);");
        outBytes.reset();
        String resp = runLine("CourseStudents(777);");
        assertEquals("No students", resp);
    }

    @Test
    void I26_courseStudents_course_not_found_returns_error() throws IOException {
        run("Login(admin, admin);");
        outBytes.reset();
        String resp = runLine("CourseStudents(9999);");
        assertTrue(resp.startsWith("Error:"), "Expected Error but got: " + resp);
    }

    @Test
    void I27_classroomSections_returns_sections_in_ascending_code() throws IOException {
        loadCR2ExampleData();
        outBytes.reset();
        String resp = runLine("ClassroomSections(G, 109);");
        String expected =
            "section 5 | tuesday | 7:10am-8:00am | 771 | programming2\n" +
            "section 6 | tuesday | 8:00am-8:50am | 771 | programming2\n" +
            "section 7 | thursday | 7:10am-8:00am | 771 | programming2\n" +
            "section 8 | thursday | 8:00am-8:50am | 771 | programming2";
        assertEquals(expected, resp);
    }

    @Test
    void I27_classroomSections_no_sections_returns_no_sections() throws IOException {
        run("Login(admin, admin);",
            "Building(G);",
            "Classroom(G, 12, 75);");
        outBytes.reset();
        String resp = runLine("ClassroomSections(G, 12);");
        assertEquals("No sections", resp);
    }

    @Test
    void I27_classroomSections_building_not_found_returns_error() throws IOException {
        run("Login(admin, admin);");
        outBytes.reset();
        String resp = runLine("ClassroomSections(Z, 1);");
        assertTrue(resp.startsWith("Error:"), "Expected Error but got: " + resp);
    }

    @Test
    void I28_passedBySemester_returns_courses_in_creation_order_with_totals() throws IOException {
        loadCR2ExampleData();
        outBytes.reset();
        String resp = runLine("PassedBySemester(4);");
        String expected =
            "771 | programming2 | passed 1 | failed 0\n" +
            "732 | Statistics1 | passed 0 | failed 0\n" +
            "795 | FormalLangs | passed 0 | failed 0\n" +
            "Total | passed 1 | failed 0";
        assertEquals(expected, resp);
    }

    @Test
    void I28_passedBySemester_no_courses_returns_no_courses_in_semester() throws IOException {
        run("Login(admin, admin);");
        outBytes.reset();
        String resp = runLine("PassedBySemester(10);");
        assertEquals("No courses in the semester", resp);
    }

    @Test
    void I28_passedBySemester_invalid_semester_returns_error() throws IOException {
        run("Login(admin, admin);");
        outBytes.reset();
        String resp = runLine("PassedBySemester(11);");
        assertTrue(resp.startsWith("Error:"), "Expected Error but got: " + resp);
    }

    // ── Balanced section choice (CR-3) ───────────────────────────────────────

    @Test
    void I18_CR3_balanced_load_777_section1_has_one_enrollment() throws IOException {
        // With the first CR-2 example data: course 777 has sections 1 and 2 (capacity 60 each).
        // Under the balanced policy: 200145785 → section 1 (tie, lowest code);
        // 200745781 → section 2 (section 1 has 1, section 2 has 0).
        // QuerySection(1) must end with 1/60.
        loadCR2ExampleData();
        outBytes.reset();
        String resp = runLine("QuerySection(1);");
        assertTrue(resp.endsWith("1/60"), "Expected QuerySection(1) to end with 1/60 but got: " + resp);
    }

    // ── Load (CR-2) ───────────────────────────────────────────────────────────

    @Test
    void I24_load_file_correct_lines_count() throws IOException {
        // Write a test file to target/
        java.nio.file.Path file = java.nio.file.Path.of("target/test-load-correct.txt");
        java.nio.file.Files.createDirectories(file.getParent());
        java.nio.file.Files.writeString(file,
            "Student(200145785, \"Fernando Marroquín\", \"Quetzaltenango\");\n" +
            "Building(G);\n",
            java.nio.charset.StandardCharsets.UTF_8);
        run("Login(admin, admin);");
        outBytes.reset();
        String resp = runLine("Load(" + file.toAbsolutePath() + ");");
        assertEquals("OK: load finished, 2 correct and 0 with errors", resp);
    }

    @Test
    void I24_load_file_error_lines_counted_correctly() throws IOException {
        java.nio.file.Path file = java.nio.file.Path.of("target/test-load-errors.txt");
        java.nio.file.Files.createDirectories(file.getParent());
        java.nio.file.Files.writeString(file,
            "Building(G);\n" +
            "Classroom(Z, 1, 30);\n" +  // line 2: building Z does not exist
            "\n" +                        // line 3: empty, does not count
            "Student(200145785, Fernando, Q);\n" +
            "Student(200145785, Other, Q);\n",  // line 5: duplicate
            java.nio.charset.StandardCharsets.UTF_8);
        run("Login(admin, admin);");
        outBytes.reset();
        String resp = runLine("Load(" + file.toAbsolutePath() + ");");
        assertTrue(resp.contains("2 correct and 2 with errors"), "Got: " + resp);
        assertTrue(resp.contains("Error in line 2:"), "Got: " + resp);
        assertTrue(resp.contains("Error in line 5:"), "Got: " + resp);
    }

    @Test
    void I24_load_file_not_allowed_instructions_give_error() throws IOException {
        java.nio.file.Path file = java.nio.file.Path.of("target/test-load-disallowed.txt");
        java.nio.file.Files.createDirectories(file.getParent());
        java.nio.file.Files.writeString(file,
            "DeleteCourse(777);\n",
            java.nio.charset.StandardCharsets.UTF_8);
        run("Login(admin, admin);");
        outBytes.reset();
        String resp = runLine("Load(" + file.toAbsolutePath() + ");");
        assertTrue(resp.contains("Error in line 1:"), "Got: " + resp);
        assertTrue(resp.contains("0 correct and 1 with errors"), "Got: " + resp);
    }

    @Test
    void I24_load_file_not_found_returns_error() throws IOException {
        run("Login(admin, admin);");
        outBytes.reset();
        String resp = runLine("Load(target/nonexistent-file.txt);");
        assertTrue(resp.startsWith("Error:"), "Expected Error but got: " + resp);
    }

    @Test
    void I24_load_staff_cannot_load_file() throws IOException {
        run("Login(admin, admin);",
            "User(staffUser, pw, staff);",
            "Logout();",
            "Login(staffUser, pw);");
        outBytes.reset();
        String resp = runLine("Load(somefile.txt);");
        assertEquals("Error: permission denied", resp);
    }

    @Test
    void I24_load_empty_file_produces_correct_summary() throws IOException {
        java.nio.file.Path file = java.nio.file.Path.of("target/test-load-empty.txt");
        java.nio.file.Files.createDirectories(file.getParent());
        java.nio.file.Files.writeString(file, "", java.nio.charset.StandardCharsets.UTF_8);
        run("Login(admin, admin);");
        outBytes.reset();
        String resp = runLine("Load(" + file.toAbsolutePath() + ");");
        assertEquals("OK: load finished, 0 correct and 0 with errors", resp);
    }
}
