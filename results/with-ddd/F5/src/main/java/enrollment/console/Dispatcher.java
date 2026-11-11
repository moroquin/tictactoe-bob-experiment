package enrollment.console;

import enrollment.application.*;
import enrollment.domain.RuleViolation;
import enrollment.domain.access.UserType;
import enrollment.domain.people.StudentNumber;
import enrollment.domain.sections.Section;

import java.util.List;
import java.util.Optional;

/**
 * Dispatches a parsed instruction to the appropriate application service and
 * returns the response string. R38, R39, R40.
 *
 * Does NOT write to output — that is done by the ConsoleLoop.
 */
public class Dispatcher {

    private final AccessService access;
    private final CampusService campus;
    private final CatalogService catalog;
    private final PeopleService people;
    private final SectionService sectionService;
    private final EnrollmentService enrollment;
    private final ReportService reportService;
    private final ResponseFormatter formatter;
    /** Set after construction to avoid circular dependency (FileLoader needs Dispatcher). */
    private FileLoader fileLoader;

    public Dispatcher(AccessService access, CampusService campus, CatalogService catalog,
                      PeopleService people, SectionService sectionService,
                      EnrollmentService enrollment, ReportService reportService,
                      ResponseFormatter formatter) {
        this.access = access;
        this.campus = campus;
        this.catalog = catalog;
        this.people = people;
        this.sectionService = sectionService;
        this.enrollment = enrollment;
        this.reportService = reportService;
        this.formatter = formatter;
    }

    /** Set the FileLoader after construction (FileLoader depends on this Dispatcher). */
    public void setFileLoader(FileLoader fileLoader) {
        this.fileLoader = fileLoader;
    }

    /**
     * Dispatch a parsed instruction and return the response.
     * Returns null to signal EXIT.
     */
    public String dispatch(InstructionParser.Parsed parsed) {
        String name = parsed.name().toLowerCase();
        List<String> v = parsed.values();

        return switch (name) {
            case "exit"              -> null;  // sentinel for exit
            case "login"             -> handleLogin(v);
            case "logout"            -> handleLogout(v);
            case "user"              -> handleUser(v);
            case "modifyuser"        -> handleModifyUser(v);
            case "deleteuser"        -> handleDeleteUser(v);
            case "queryuser"         -> handleQueryUser(v);
            case "building"          -> handleBuilding(v);
            case "modifybuilding"    -> handleModifyBuilding(v);
            case "deletebuilding"    -> handleDeleteBuilding(v);
            case "querybuilding"     -> handleQueryBuilding(v);
            case "classroom"         -> handleClassroom(v);
            case "modifyclassroom"   -> handleModifyClassroom(v);
            case "deleteclassroom"   -> handleDeleteClassroom(v);
            case "course"            -> handleCourse(v);
            case "modifycourse"      -> handleModifyCourse(v);
            case "deletecourse"      -> handleDeleteCourse(v);
            case "querycourse"       -> handleQueryCourse(v);
            case "student"           -> handleStudent(v);
            case "modifystudent"     -> handleModifyStudent(v);
            case "deletestudent"     -> handleDeleteStudent(v);
            case "querystudent"      -> handleQueryStudent(v);
            case "professor"         -> handleProfessor(v);
            case "modifyprofessor"   -> handleModifyProfessor(v);
            case "deleteprofessor"   -> handleDeleteProfessor(v);
            case "queryprofessor"    -> handleQueryProfessor(v);
            case "section"           -> handleSection(v);
            case "modifysection"     -> handleModifySection(v);
            case "deletesection"     -> handleDeleteSection(v);
            case "querysection"      -> handleQuerySection(v);
            case "enroll"            -> handleEnroll(v);
            case "enrollme"          -> handleEnrollMe(v);
            case "changegrades"      -> handleChangeGrades(v);
            case "unenroll"          -> handleUnenroll(v);
            case "studentcourses"    -> handleStudentCourses(v);
            case "coursestudents"    -> handleCourseStudents(v);
            case "classroomsections" -> handleClassroomSections(v);
            case "passedbysemester"  -> handlePassedBySemester(v);
            case "load"              -> handleLoad(v);
            default                  -> "Error: invalid instruction";
        };
    }

    // ── Session ───────────────────────────────────────────────────────────────

    private String handleLogin(List<String> v) {
        requireArity(v, 2);
        access.login(v.get(0), v.get(1));
        return "OK: logged in as " + v.get(0);
    }

    private String handleLogout(List<String> v) {
        requireArity(v, 0);
        access.logout();
        return "OK: logged out";
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    private String handleUser(List<String> v) {
        // User(name, password, type[, studentNumber])
        if (v.size() == 3) {
            UserType type = UserType.parse(v.get(2));
            access.createUser(v.get(0), v.get(1), type, Optional.empty());
            return "OK: user created";
        } else if (v.size() == 4) {
            UserType type = UserType.parse(v.get(2));
            long sn = parseNumber(v.get(3));
            access.createUser(v.get(0), v.get(1), type, Optional.of(new StudentNumber(sn)));
            return "OK: user created";
        } else {
            throw new RuleViolation("invalid instruction");
        }
    }

    private String handleModifyUser(List<String> v) {
        // ModifyUser(id, name, password, type[, studentNumber])
        if (v.size() == 4) {
            long id = parseNumber(v.get(0));
            UserType type = UserType.parse(v.get(3));
            access.modifyUser(id, v.get(1), v.get(2), type, Optional.empty());
            return "OK: user modified";
        } else if (v.size() == 5) {
            long id = parseNumber(v.get(0));
            UserType type = UserType.parse(v.get(3));
            long sn = parseNumber(v.get(4));
            access.modifyUser(id, v.get(1), v.get(2), type, Optional.of(new StudentNumber(sn)));
            return "OK: user modified";
        } else {
            throw new RuleViolation("invalid instruction");
        }
    }

    private String handleDeleteUser(List<String> v) {
        requireArity(v, 1);
        long id = parseNumber(v.get(0));
        access.deleteUser(id);
        return "OK: user deleted";
    }

    private String handleQueryUser(List<String> v) {
        requireArity(v, 1);
        long id = parseNumber(v.get(0));
        var user = access.queryUser(id);
        return formatter.formatUser(user);
    }

    // ── Buildings ─────────────────────────────────────────────────────────────

    private String handleBuilding(List<String> v) {
        requireArity(v, 1);
        campus.createBuilding(v.get(0));
        return "OK: building created";
    }

    private String handleModifyBuilding(List<String> v) {
        requireArity(v, 2);
        campus.modifyBuilding(v.get(0), v.get(1));
        return "OK: building modified";
    }

    private String handleDeleteBuilding(List<String> v) {
        requireArity(v, 1);
        campus.deleteBuilding(v.get(0));
        return "OK: building deleted";
    }

    private String handleQueryBuilding(List<String> v) {
        requireArity(v, 1);
        var b = campus.queryBuilding(v.get(0));
        return formatter.formatBuilding(b);
    }

    // ── Classrooms ────────────────────────────────────────────────────────────

    private String handleClassroom(List<String> v) {
        requireArity(v, 3);
        int number = (int) parseNumber(v.get(1));
        int capacity = (int) parseNumber(v.get(2));
        campus.createClassroom(v.get(0), number, capacity);
        return "OK: classroom created";
    }

    private String handleModifyClassroom(List<String> v) {
        requireArity(v, 3);
        int number = (int) parseNumber(v.get(1));
        int capacity = (int) parseNumber(v.get(2));
        campus.modifyClassroom(v.get(0), number, capacity);
        return "OK: classroom modified";
    }

    private String handleDeleteClassroom(List<String> v) {
        requireArity(v, 2);
        int number = (int) parseNumber(v.get(1));
        campus.deleteClassroom(v.get(0), number);
        return "OK: classroom deleted";
    }

    // ── Courses ───────────────────────────────────────────────────────────────

    private String handleCourse(List<String> v) {
        requireArity(v, 4);
        long code = parseNumber(v.get(0));
        int semester = (int) parseNumber(v.get(2));
        int credits = (int) parseNumber(v.get(3));
        catalog.createCourse(code, v.get(1), semester, credits);
        return "OK: course created";
    }

    private String handleModifyCourse(List<String> v) {
        requireArity(v, 4);
        long code = parseNumber(v.get(0));
        int semester = (int) parseNumber(v.get(2));
        int credits = (int) parseNumber(v.get(3));
        catalog.modifyCourse(code, v.get(1), semester, credits);
        return "OK: course modified";
    }

    private String handleDeleteCourse(List<String> v) {
        requireArity(v, 1);
        long code = parseNumber(v.get(0));
        catalog.deleteCourse(code);
        return "OK: course deleted";
    }

    private String handleQueryCourse(List<String> v) {
        requireArity(v, 1);
        long code = parseNumber(v.get(0));
        var course = catalog.queryCourse(code);
        return formatter.formatCourse(course);
    }

    // ── Students ──────────────────────────────────────────────────────────────

    private String handleStudent(List<String> v) {
        requireArity(v, 3);
        long number = parseNumber(v.get(0));
        people.createStudent(number, v.get(1), v.get(2));
        return "OK: student created";
    }

    private String handleModifyStudent(List<String> v) {
        requireArity(v, 3);
        long number = parseNumber(v.get(0));
        people.modifyStudent(number, v.get(1), v.get(2));
        return "OK: student modified";
    }

    private String handleDeleteStudent(List<String> v) {
        requireArity(v, 1);
        long number = parseNumber(v.get(0));
        people.deleteStudent(number);
        return "OK: student deleted";
    }

    private String handleQueryStudent(List<String> v) {
        requireArity(v, 1);
        long number = parseNumber(v.get(0));
        var student = people.queryStudent(number);
        return formatter.formatStudent(student);
    }

    // ── Professors ────────────────────────────────────────────────────────────

    private String handleProfessor(List<String> v) {
        requireArity(v, 3);
        long id = parseNumber(v.get(0));
        people.createProfessor(id, v.get(1), v.get(2));
        return "OK: professor created";
    }

    private String handleModifyProfessor(List<String> v) {
        requireArity(v, 3);
        long id = parseNumber(v.get(0));
        people.modifyProfessor(id, v.get(1), v.get(2));
        return "OK: professor modified";
    }

    private String handleDeleteProfessor(List<String> v) {
        requireArity(v, 1);
        long id = parseNumber(v.get(0));
        people.deleteProfessor(id);
        return "OK: professor deleted";
    }

    private String handleQueryProfessor(List<String> v) {
        requireArity(v, 1);
        long id = parseNumber(v.get(0));
        var professor = people.queryProfessor(id);
        return formatter.formatProfessor(professor);
    }

    // ── Sections ──────────────────────────────────────────────────────────────

    private String handleSection(List<String> v) {
        requireArity(v, 7);
        long code = parseNumber(v.get(0));
        String period = v.get(1);
        String day = v.get(2);
        long courseCode = parseNumber(v.get(3));
        int classroomNumber = (int) parseNumber(v.get(4));
        String building = v.get(5);
        long professorId = parseNumber(v.get(6));
        sectionService.createSection(code, period, day, courseCode, classroomNumber, building, professorId);
        return "OK: section created";
    }

    private String handleModifySection(List<String> v) {
        requireArity(v, 7);
        long code = parseNumber(v.get(0));
        String period = v.get(1);
        String day = v.get(2);
        long courseCode = parseNumber(v.get(3));
        int classroomNumber = (int) parseNumber(v.get(4));
        String building = v.get(5);
        long professorId = parseNumber(v.get(6));
        sectionService.modifySection(code, period, day, courseCode, classroomNumber, building, professorId);
        return "OK: section modified";
    }

    private String handleDeleteSection(List<String> v) {
        requireArity(v, 1);
        long code = parseNumber(v.get(0));
        sectionService.deleteSection(code);
        return "OK: section deleted";
    }

    private String handleQuerySection(List<String> v) {
        requireArity(v, 1);
        long code = parseNumber(v.get(0));
        var section = sectionService.querySection(code);
        return formatter.formatSection(section);
    }

    // ── Enrollments ───────────────────────────────────────────────────────────

    private String handleEnroll(List<String> v) {
        requireArity(v, 4);
        long studentNumber = parseNumber(v.get(0));
        long courseCode = parseNumber(v.get(1));
        int coursework = (int) parseNumber(v.get(2));
        int finalExam = (int) parseNumber(v.get(3));
        Section section = enrollment.enroll(studentNumber, courseCode, coursework, finalExam);
        return "OK: " + studentNumber + " enrolled in course " + courseCode + " in section " + section.code();
    }

    private String handleEnrollMe(List<String> v) {
        requireArity(v, 1);
        long courseCode = parseNumber(v.get(0));
        Section section = enrollment.enrollMe(courseCode);
        // Need to get the student number from the session
        long studentNumber = access.session().user()
                .flatMap(u -> u.studentNumber())
                .map(sn -> sn.value())
                .orElse(0L);
        return "OK: " + studentNumber + " enrolled in course " + courseCode + " in section " + section.code();
    }

    private String handleChangeGrades(List<String> v) {
        requireArity(v, 4);
        long studentNumber = parseNumber(v.get(0));
        long courseCode = parseNumber(v.get(1));
        int coursework = (int) parseNumber(v.get(2));
        int finalExam = (int) parseNumber(v.get(3));
        enrollment.changeGrades(studentNumber, courseCode, coursework, finalExam);
        return "OK: grades changed";
    }

    private String handleUnenroll(List<String> v) {
        requireArity(v, 2);
        long studentNumber = parseNumber(v.get(0));
        long courseCode = parseNumber(v.get(1));
        enrollment.unenroll(studentNumber, courseCode);
        return "OK: unenrolled";
    }

    // ── Reports (CR-2) ───────────────────────────────────────────────────────

    private String handleStudentCourses(List<String> v) {
        requireArity(v, 1);
        long studentNumber = parseNumber(v.get(0));
        var rows = reportService.studentCourses(studentNumber);
        return formatter.formatStudentCourses(rows);
    }

    private String handleCourseStudents(List<String> v) {
        requireArity(v, 1);
        long courseCode = parseNumber(v.get(0));
        var rows = reportService.courseStudents(courseCode);
        return formatter.formatCourseStudents(rows);
    }

    private String handleClassroomSections(List<String> v) {
        requireArity(v, 2);
        String buildingName = v.get(0);
        int classroomNumber = (int) parseNumber(v.get(1));
        var rows = reportService.classroomSections(buildingName, classroomNumber);
        return formatter.formatClassroomSections(rows);
    }

    private String handlePassedBySemester(List<String> v) {
        requireArity(v, 1);
        int semester = (int) parseNumber(v.get(0));
        var rows = reportService.passedBySemester(semester);
        return formatter.formatPassedBySemester(rows);
    }

    // ── Load (CR-2) ───────────────────────────────────────────────────────────

    /**
     * Load(path) — admin only (I24).
     * The FileLoader writes the output directly; we return an empty string to signal no
     * additional output from the dispatcher. The caller (ConsoleLoop) will still print
     * that string, so we return null-like… actually the FileLoader writes its own lines
     * including the final OK. We must not let the ConsoleLoop print anything extra.
     *
     * Solution: return a sentinel that ConsoleLoop treats as "output already written."
     * We return an empty string "" which ConsoleLoop will println — producing a blank line.
     * That's wrong. Instead, the Load handler should return null to suppress output from
     * ConsoleLoop. But null means Exit...
     *
     * Better: FileLoader returns the full output as a multi-line String, and ConsoleLoop
     * prints it. But FileLoader needs to print errors mid-stream.
     *
     * Actual design (D19): FileLoader.load() writes directly to out; the dispatcher
     * returns a special marker that ConsoleLoop must not print. We use an empty string
     * and ConsoleLoop skips printing empty strings. OR: we make the Load response
     * completely written by the FileLoader and the dispatcher returns null with a
     * different convention.
     *
     * Simplest workable solution: return a special sentinel constant that ConsoleLoop
     * recognizes as "already printed, do not println." We add ALREADY_PRINTED = "".
     * ConsoleLoop already does out.println(response); an empty println() would add
     * a blank line — not acceptable.
     *
     * Real solution: FileLoader writes error lines + final OK to out. Dispatcher
     * returns the LOAD_HANDLED sentinel (empty string ""). ConsoleLoop: if
     * response is "" skip println.
     */
    static final String LOAD_HANDLED = "\u0000"; // NUL sentinel: output already written

    private String handleLoad(List<String> v) {
        requireArity(v, 1);
        String path = v.get(0);
        fileLoader.load(path);
        return LOAD_HANDLED;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void requireArity(List<String> v, int expected) {
        if (v.size() != expected)
            throw new RuleViolation("invalid instruction");
    }

    /**
     * Parse a number value from a string. Throws "invalid instruction" if not a valid
     * unsigned integer of up to 10 digits (R34).
     */
    static long parseNumber(String s) {
        String t = s.strip();
        if (t.isEmpty() || t.length() > 10)
            throw new RuleViolation("invalid instruction");
        for (char c : t.toCharArray()) {
            if (c < '0' || c > '9')
                throw new RuleViolation("invalid instruction");
        }
        try {
            return Long.parseLong(t);
        } catch (NumberFormatException e) {
            throw new RuleViolation("invalid instruction");
        }
    }
}
