package enrollment;

import enrollment.console.Console;
import enrollment.service.EnrollmentService;
import enrollment.store.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Load instruction and the four CR-2 reports,
 * exercised through the Console layer.
 */
class LoadAndReportConsoleTest {

    @TempDir
    Path tempDir;

    private Store store;
    private EnrollmentService service;

    /** Runs the given console input and returns the output lines. */
    private String runConsole(String input) {
        store = new Store();
        service = new EnrollmentService(store);
        var in = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8));
        var outBuf = new StringWriter();
        var out = new PrintWriter(outBuf, true);
        var err = new PrintWriter(new StringWriter()); // discard prompts
        new Console(service, in, out, err).run();
        return outBuf.toString();
    }

    // ────────────────────────────────────── Load: happy path ──

    @Test
    void load_all_correct() throws IOException {
        Path file = tempDir.resolve("data.txt");
        Files.writeString(file,
                "Student(100, \"Alice\", \"City\");\n" +
                "Building(G);\n" +
                "Classroom(G, 5, 30);\n" +
                "Professor(1, \"Prof\", \"Addr\");\n" +
                "Course(10, \"calc\", 3, 4);\n" +
                "Section(1, \"7:10am-8:00am\", \"monday\", 10, 5, G, 1);\n" +
                "Enroll(100, 10, 30, 20);\n",
                StandardCharsets.UTF_8);

        String output = runConsole(
                "Login(admin, admin);\n" +
                "Load(\"" + file + "\");\n");

        assertTrue(output.contains("OK: load finished, 7 correct and 0 with errors"), output);
    }

    @Test
    void load_empty_lines_not_counted() throws IOException {
        Path file = tempDir.resolve("data.txt");
        Files.writeString(file,
                "Building(G);\n" +
                "\n" +
                "Building(D);\n",
                StandardCharsets.UTF_8);

        String output = runConsole(
                "Login(admin, admin);\n" +
                "Load(\"" + file + "\");\n");

        assertTrue(output.contains("OK: load finished, 2 correct and 0 with errors"), output);
    }

    // ────────────────────────────────────── Load: errors ──

    @Test
    void load_nonexistent_file_reports_error() {
        String output = runConsole(
                "Login(admin, admin);\n" +
                "Load(\"/nonexistent/file/path.txt\");\n");

        assertTrue(output.contains("Error:"), output);
        // should NOT contain "OK: load finished"
        assertFalse(output.contains("OK: load finished"), output);
    }

    @Test
    void load_disallowed_instruction_is_error_on_that_line() throws IOException {
        Path file = tempDir.resolve("data.txt");
        Files.writeString(file,
                "Building(G);\n" +
                "DeleteBuilding(G);\n" +  // not allowed
                "Building(D);\n",
                StandardCharsets.UTF_8);

        String output = runConsole(
                "Login(admin, admin);\n" +
                "Load(\"" + file + "\");\n");

        assertTrue(output.contains("Error in line 2:"), output);
        assertTrue(output.contains("OK: load finished, 2 correct and 1 with errors"), output);
    }

    @Test
    void load_invalid_syntax_is_error_on_that_line() throws IOException {
        Path file = tempDir.resolve("data.txt");
        Files.writeString(file,
                "Building(G);\n" +
                "Professor(12345, \"Prof1\", \"City\";\n" +  // missing closing paren
                "Building(D);\n",
                StandardCharsets.UTF_8);

        String output = runConsole(
                "Login(admin, admin);\n" +
                "Load(\"" + file + "\");\n");

        assertTrue(output.contains("Error in line 2:"), output);
        assertTrue(output.contains("OK: load finished, 2 correct and 1 with errors"), output);
    }

    @Test
    void load_domain_error_counted_as_error() throws IOException {
        Path file = tempDir.resolve("data.txt");
        Files.writeString(file,
                "Building(G);\n" +
                "Classroom(Z, 1, 30);\n",  // building Z doesn't exist
                StandardCharsets.UTF_8);

        String output = runConsole(
                "Login(admin, admin);\n" +
                "Load(\"" + file + "\");\n");

        assertTrue(output.contains("Error in line 2:"), output);
        assertTrue(output.contains("OK: load finished, 1 correct and 1 with errors"), output);
    }

    @Test
    void load_line_numbers_counted_from_1_including_empty() throws IOException {
        Path file = tempDir.resolve("data.txt");
        // line 1: empty, line 2: Building(G), line 3: Classroom(Z,...) [error], line 4: Building(D)
        Files.writeString(file,
                "\n" +
                "Building(G);\n" +
                "Classroom(Z, 1, 30);\n" +
                "Building(D);\n",
                StandardCharsets.UTF_8);

        String output = runConsole(
                "Login(admin, admin);\n" +
                "Load(\"" + file + "\");\n");

        assertTrue(output.contains("Error in line 3:"), output);
        assertTrue(output.contains("OK: load finished, 2 correct and 1 with errors"), output);
    }

    @Test
    void load_continues_after_error() throws IOException {
        Path file = tempDir.resolve("data.txt");
        Files.writeString(file,
                "Building(G);\n" +
                "Building(G);\n" +  // duplicate → error
                "Building(D);\n",   // still runs
                StandardCharsets.UTF_8);

        String output = runConsole(
                "Login(admin, admin);\n" +
                "Load(\"" + file + "\");\n");

        assertTrue(output.contains("Error in line 2:"), output);
        assertTrue(output.contains("OK: load finished, 2 correct and 1 with errors"), output);
        // D should have been created
        assertNotNull(store.getBuildingByName("D"));
    }

    @Test
    void load_requires_admin_permission() {
        String output = runConsole(
                "Login(admin, admin);\n" +
                "User(s1, \"pw\", staff);\n" +
                "Logout();\n" +
                "Login(s1, pw);\n" +
                "Load(\"/any/path.txt\");\n");

        assertTrue(output.contains("Error: permission denied"), output);
    }

    // ────────────────────────────────────── Load: full example from CR-2 spec ──

    @Test
    void load_full_example_42_correct() throws IOException {
        Path file = tempDir.resolve("full.txt");
        Files.writeString(file,
                "Student(200145785, \"Fernando Marroquín\", \"Quetzaltenango\");\n" +
                "Student(200745781, \"Alfonso Antonio\", \"Cantel\");\n" +
                "Student(200478542, \"Mario Morales\", \"La Esperanza\");\n" +
                "Student(201012362, \"Ulises López\", \"Salcajá\");\n" +
                "\n" +
                "User(Secretary, \"murphy1\", staff);\n" +
                "User(Control, \"control123\", staff);\n" +
                "User(FMarroquín, \"fffmmm\", student, 200145785);\n" +
                "User(Ulises, \"uliuli\", student, 201012362);\n" +
                "\n" +
                "Professor(12345, \"Prof1\", \"City\");\n" +
                "Professor(47854, \"Prof2\", \"City\");\n" +
                "Professor(12445, \"Prof3\", \"City\");\n" +
                "Professor(47354, \"Prof4\", \"City\");\n" +
                "Professor(12365, \"Prof5\", \"City\");\n" +
                "\n" +
                "Building(\"G\");\n" +
                "Building(\"E\");\n" +
                "Building(\"D\");\n" +
                "\n" +
                "Classroom(\"G\", 12, 75);\n" +
                "Classroom (\"E\", 10, 25);\n" +
                "Classroom (\"D\", 5, 125);\n" +
                "Classroom(\"G\", 105, 60);\n" +
                "Classroom(\"G\", 109, 40);\n" +
                "Classroom(\"G\", 110, 35);\n" +
                "\n" +
                "Course (777,\"compilers1\", 5,4);\n" +
                "Course (771,\"programming2\", 4, 4);\n" +
                "Course (779,\"Computer architecture and assemblers 2\", 7, 4);\n" +
                "Course (732,\"Statistics 1\", 4, 4);\n" +
                "Course (795,\"Formal languages\", 4, 4);\n" +
                "\n" +
                "Section(1,\"7:10am-8:00am\",\"monday\",777,105,G,12345);\n" +
                "Section(2,\"8:00am-8:50am\",\"monday\",777,105,G,12345);\n" +
                "Section(3,\"7:10am-8:00am\",\"friday\",777,105,G,47854);\n" +
                "Section(4,\"8:00am-8:50am\",\"friday\",777,105,G,47854);\n" +
                "Section(5,\"7:10am-8:00am\",\"tuesday\",771,109,G,12345);\n" +
                "Section(6,\"8:00am-8:50am\",\"tuesday\",771,109,G,12345);\n" +
                "Section(7,\"7:10am-8:00am\",\"thursday\",771,109,G,47854);\n" +
                "Section(8,\"8:00am-8:50am\",\"thursday\",771,109,G,47854);\n" +
                "Section(9,\"9:10am-10:00am\",\"tuesday\",779,110,G,12445);\n" +
                "Section(10,\"10:00am-10:50am\",\"tuesday\",779,110,G,12445);\n" +
                "Section(11,\"9:10am-10:00am\",\"thursday\",779,110,G,12365);\n" +
                "Section(12,\"10:00am-10:50am\",\"thursday\",779,110,G,12365);\n" +
                "\n" +
                "Enroll (200145785, 777, 30, 25);\n" +
                "Enroll (200745781, 777, 60, 10);\n" +
                "Enroll (200478542, 771, 70, 20);\n",
                StandardCharsets.UTF_8);

        String output = runConsole(
                "Login(admin, admin);\n" +
                "Load(\"" + file + "\");\n");

        assertTrue(output.contains("OK: load finished, 42 correct and 0 with errors"), output);

        // CR-3: balanced sections — first student of 777 in section 1, second in section 2
        assertEquals(1L, store.findEnrollment(200145785L, 777L).getSection().getCode());
        assertEquals(2L, store.findEnrollment(200745781L, 777L).getSection().getCode());
        // Student of 771 in section 5
        assertEquals(5L, store.findEnrollment(200478542L, 771L).getSection().getCode());
    }

    // ────────────────────────────────────── Report: StudentCourses ──

    @Test
    void report_studentCourses_output_format() throws IOException {
        Path file = tempDir.resolve("data.txt");
        Files.writeString(file,
                "Student(200145785, \"Fernando Marroquín\", \"Quetzaltenango\");\n" +
                "Building(G);\n" +
                "Classroom(G, 105, 60);\n" +
                "Professor(12345, \"Prof1\", \"City\");\n" +
                "Course(777, \"compilers1\", 5, 4);\n" +
                "Section(1, \"7:10am-8:00am\", \"monday\", 777, 105, G, 12345);\n" +
                "Enroll(200145785, 777, 30, 25);\n",
                StandardCharsets.UTF_8);

        String output = runConsole(
                "Login(admin, admin);\n" +
                "Load(\"" + file + "\");\n" +
                "StudentCourses(200145785);\n");

        assertTrue(output.contains("777 | compilers1 | section 1 | coursework 30 | final 25 | failed"), output);
    }

    @Test
    void report_studentCourses_no_courses() {
        String output = runConsole(
                "Login(admin, admin);\n" +
                "Student(1, \"Alice\", \"City\");\n" +
                "StudentCourses(1);\n");
        assertTrue(output.contains("No courses"), output);
    }

    @Test
    void report_studentCourses_student_not_found() {
        String output = runConsole(
                "Login(admin, admin);\n" +
                "StudentCourses(999);\n");
        assertTrue(output.contains("Error:"), output);
    }

    // ────────────────────────────────────── Report: CourseStudents ──

    @Test
    void report_courseStudents_format() throws IOException {
        Path file = tempDir.resolve("data.txt");
        Files.writeString(file,
                "Student(200145785, \"Fernando Marroquín\", \"Quetzaltenango\");\n" +
                "Student(200745781, \"Alfonso Antonio\", \"Cantel\");\n" +
                "Building(G);\n" +
                "Classroom(G, 105, 60);\n" +
                "Professor(12345, \"Prof1\", \"City\");\n" +
                "Course(777, \"compilers1\", 5, 4);\n" +
                "Section(1, \"7:10am-8:00am\", \"monday\", 777, 105, G, 12345);\n" +
                "Enroll(200145785, 777, 30, 25);\n" +
                "Enroll(200745781, 777, 60, 10);\n",
                StandardCharsets.UTF_8);

        String output = runConsole(
                "Login(admin, admin);\n" +
                "Load(\"" + file + "\");\n" +
                "CourseStudents(777);\n");

        assertTrue(output.contains("200145785 | Fernando Marroquín | section 1 | coursework 30 | final 25 | failed"), output);
        assertTrue(output.contains("200745781 | Alfonso Antonio | section 1 | coursework 60 | final 10 | passed"), output);
    }

    // ────────────────────────────────────── Report: ClassroomSections ──

    @Test
    void report_classroomSections_format() throws IOException {
        Path file = tempDir.resolve("data.txt");
        Files.writeString(file,
                "Building(G);\n" +
                "Classroom(G, 109, 40);\n" +
                "Professor(12345, \"Prof1\", \"City\");\n" +
                "Professor(47854, \"Prof2\", \"City\");\n" +
                "Course(771, \"programming2\", 4, 4);\n" +
                "Section(5, \"7:10am-8:00am\", \"tuesday\", 771, 109, G, 12345);\n" +
                "Section(6, \"8:00am-8:50am\", \"tuesday\", 771, 109, G, 12345);\n" +
                "Section(7, \"7:10am-8:00am\", \"thursday\", 771, 109, G, 47854);\n" +
                "Section(8, \"8:00am-8:50am\", \"thursday\", 771, 109, G, 47854);\n",
                StandardCharsets.UTF_8);

        String output = runConsole(
                "Login(admin, admin);\n" +
                "Load(\"" + file + "\");\n" +
                "ClassroomSections(G, 109);\n");

        assertTrue(output.contains("section 5 | tuesday | 7:10am-8:00am | 771 | programming2"), output);
        assertTrue(output.contains("section 6 | tuesday | 8:00am-8:50am | 771 | programming2"), output);
        assertTrue(output.contains("section 7 | thursday | 7:10am-8:00am | 771 | programming2"), output);
        assertTrue(output.contains("section 8 | thursday | 8:00am-8:50am | 771 | programming2"), output);
    }

    // ────────────────────────────────────── Report: PassedBySemester ──

    @Test
    void report_passedBySemester_format() throws IOException {
        Path file = tempDir.resolve("data.txt");
        Files.writeString(file,
                "Building(G);\n" +
                "Classroom(G, 109, 40);\n" +
                "Professor(12345, \"Prof1\", \"City\");\n" +
                "Course(771, \"programming2\", 4, 4);\n" +
                "Course(732, \"Statistics 1\", 4, 4);\n" +
                "Course(795, \"Formal languages\", 4, 4);\n" +
                "Section(5, \"7:10am-8:00am\", \"tuesday\", 771, 109, G, 12345);\n" +
                "Student(200478542, \"Mario Morales\", \"La Esperanza\");\n" +
                "Enroll(200478542, 771, 70, 20);\n",
                StandardCharsets.UTF_8);

        String output = runConsole(
                "Login(admin, admin);\n" +
                "Load(\"" + file + "\");\n" +
                "PassedBySemester(4);\n");

        assertTrue(output.contains("771 | programming2 | passed 1 | failed 0"), output);
        assertTrue(output.contains("732 | Statistics 1 | passed 0 | failed 0"), output);
        assertTrue(output.contains("795 | Formal languages | passed 0 | failed 0"), output);
        assertTrue(output.contains("Total | passed 1 | failed 0"), output);
    }
}
