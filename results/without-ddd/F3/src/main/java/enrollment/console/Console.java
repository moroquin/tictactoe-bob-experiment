package enrollment.console;

import enrollment.model.UserType;
import enrollment.service.EnrollmentService;
import enrollment.service.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Reads lines, parses them with Parser, dispatches to EnrollmentService,
 * and writes responses.
 *
 * All standard-input / standard-output / standard-error access is here
 * (plus Main), per ARCHITECTURE.md.
 */
public class Console {

    private final EnrollmentService service;
    private final BufferedReader in;
    private final PrintWriter out;
    private final PrintWriter err;

    public Console(EnrollmentService service, BufferedReader in, PrintWriter out, PrintWriter err) {
        this.service = service;
        this.in = in;
        this.out = out;
        this.err = err;
    }

    public void run() {
        String line;
        try {
            while ((line = in.readLine()) != null) {
                err.print("> ");
                err.flush();
                String response = handle(line);
                if (response == null) break; // Exit
                if (!response.isEmpty()) out.println(response);
            }
        } catch (IOException e) {
            // end of input
        }
    }

    /** Returns null to signal Exit, "" for blank lines, otherwise the response. */
    private String handle(String line) {
        Parser.Instruction instr;
        try {
            instr = Parser.parse(line);
        } catch (Parser.ParseException e) {
            return "Error: invalid instruction";
        }
        if (instr == null) return ""; // blank line — R37

        String name = instr.name();
        List<String> args = instr.args();

        // R40: unknown name → invalid instruction
        return switch (name) {
            case "exit"          -> handleExit(args);
            case "login"         -> handleLogin(args);
            case "logout"        -> requireSession(name, () -> handleLogout(args));
            case "user"          -> requireSession(name, () -> handleUser(args));
            case "modifyuser"    -> requireSession(name, () -> handleModifyUser(args));
            case "deleteuser"    -> requireSession(name, () -> handleDeleteUser(args));
            case "queryuser"     -> requireSession(name, () -> handleQueryUser(args));
            case "building"      -> requireSession(name, () -> handleBuilding(args));
            case "modifybuilding"-> requireSession(name, () -> handleModifyBuilding(args));
            case "deletebuilding"-> requireSession(name, () -> handleDeleteBuilding(args));
            case "querybuilding" -> requireSession(name, () -> handleQueryBuilding(args));
            case "classroom"     -> requireSession(name, () -> handleClassroom(args));
            case "modifyclassroom" -> requireSession(name, () -> handleModifyClassroom(args));
            case "deleteclassroom" -> requireSession(name, () -> handleDeleteClassroom(args));
            case "course"        -> requireSession(name, () -> handleCourse(args));
            case "modifycourse"  -> requireSession(name, () -> handleModifyCourse(args));
            case "deletecourse"  -> requireSession(name, () -> handleDeleteCourse(args));
            case "querycourse"   -> requireSession(name, () -> handleQueryCourse(args));
            case "student"       -> requireSession(name, () -> handleStudent(args));
            case "modifystudent" -> requireSession(name, () -> handleModifyStudent(args));
            case "deletestudent" -> requireSession(name, () -> handleDeleteStudent(args));
            case "querystudent"  -> requireSession(name, () -> handleQueryStudent(args));
            case "professor"     -> requireSession(name, () -> handleProfessor(args));
            case "modifyprofessor" -> requireSession(name, () -> handleModifyProfessor(args));
            case "deleteprofessor" -> requireSession(name, () -> handleDeleteProfessor(args));
            case "queryprofessor"  -> requireSession(name, () -> handleQueryProfessor(args));
            case "section"       -> requireSession(name, () -> handleSection(args));
            case "modifysection" -> requireSession(name, () -> handleModifySection(args));
            case "deletesection" -> requireSession(name, () -> handleDeleteSection(args));
            case "querysection"  -> requireSession(name, () -> handleQuerySection(args));
            case "enroll"        -> requireSession(name, () -> handleEnroll(args));
            case "enrollme"      -> requireSession(name, () -> handleEnrollMe(args));
            case "changegrades"  -> requireSession(name, () -> handleChangeGrades(args));
            case "unenroll"      -> requireSession(name, () -> handleUnenroll(args));
            case "load"          -> requireSession(name, () -> handleLoad(args));
            case "studentcourses"   -> requireSession(name, () -> handleStudentCourses(args));
            case "coursestudents"   -> requireSession(name, () -> handleCourseStudents(args));
            case "classroomsections" -> requireSession(name, () -> handleClassroomSections(args));
            case "passedbysemester" -> requireSession(name, () -> handlePassedBySemester(args));
            default              -> "Error: invalid instruction";
        };
    }

    // ────────────────────────────────────────────── session helpers ──

    /**
     * R8 order: (1) valid instruction (done by the switch), (2) session open,
     * (3) permission, (4) entity rules.
     */
    private String requireSession(String instrName, java.util.function.Supplier<String> action) {
        if (!service.hasSession()) return "Error: log in first";
        return action.get();
    }

    private String requirePermission(boolean allowed, java.util.function.Supplier<String> action) {
        if (!allowed) return "Error: permission denied";
        return action.get();
    }

    private boolean isAdmin() {
        return service.currentUser() != null && service.currentUser().getType() == UserType.ADMIN;
    }

    private boolean isAdminOrStaff() {
        if (service.currentUser() == null) return false;
        return service.currentUser().getType() == UserType.ADMIN
                || service.currentUser().getType() == UserType.STAFF;
    }

    private boolean isStudent() {
        return service.currentUser() != null && service.currentUser().getType() == UserType.STUDENT;
    }

    // ────────────────────────────────────────────── response format ──

    private static String respond(Result r) {
        return r.ok() ? "OK: " + r.message() : "Error: " + r.message();
    }

    // ────────────────────────────────────────────── argument helpers ──

    /** Parses an unsigned integer arg, throwing ParseException if invalid (R34). */
    private static long parseNumber(String s) {
        if (s == null || s.isEmpty()) throw new Parser.ParseException("missing number");
        for (char c : s.toCharArray()) {
            if (c < '0' || c > '9') throw new Parser.ParseException("not a number: " + s);
        }
        if (s.length() > 10) throw new Parser.ParseException("number too large: " + s);
        long v = Long.parseLong(s);
        if (v > 9_999_999_999L) throw new Parser.ParseException("number too large: " + s);
        return v;
    }

    private static int parseIntArg(String s) {
        long v = parseNumber(s);
        return (int) v;
    }

    /** Validates arg count; throws ParseException if wrong. */
    private static void requireArgs(List<String> args, int exact) {
        if (args.size() != exact) throw new Parser.ParseException("wrong number of arguments");
    }

    private static void requireArgsBetween(List<String> args, int min, int max) {
        if (args.size() < min || args.size() > max) throw new Parser.ParseException("wrong number of arguments");
    }

    // ────────────────────────────────────────────── handlers ──

    private String handleExit(List<String> args) {
        if (args.size() != 0) return "Error: invalid instruction";
        return null; // signal to stop
    }

    private String handleLogin(List<String> args) {
        if (args.size() != 2) return "Error: invalid instruction";
        return respond(service.login(args.get(0), args.get(1)));
    }

    private String handleLogout(List<String> args) {
        if (args.size() != 0) return "Error: invalid instruction";
        return respond(service.logout());
    }

    // ── users ──

    private String handleUser(List<String> args) {
        return requirePermission(isAdmin(), () -> {
            if (args.size() < 3 || args.size() > 4) return "Error: invalid instruction";
            String name = args.get(0), pass = args.get(1), typeStr = args.get(2);
            UserType type = UserType.parse(typeStr);
            if (type == null) return "Error: invalid instruction";
            Long studentNumber = null;
            if (args.size() == 4) {
                try { studentNumber = parseNumber(args.get(3)); }
                catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            }
            return respond(service.createUser(name, pass, type, studentNumber));
        });
    }

    private String handleModifyUser(List<String> args) {
        return requirePermission(isAdmin(), () -> {
            if (args.size() < 4 || args.size() > 5) return "Error: invalid instruction";
            int id;
            try { id = parseIntArg(args.get(0)); }
            catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            String name = args.get(1), pass = args.get(2), typeStr = args.get(3);
            UserType type = UserType.parse(typeStr);
            if (type == null) return "Error: invalid instruction";
            Long studentNumber = null;
            if (args.size() == 5) {
                try { studentNumber = parseNumber(args.get(4)); }
                catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            }
            return respond(service.modifyUser(id, name, pass, type, studentNumber));
        });
    }

    private String handleDeleteUser(List<String> args) {
        return requirePermission(isAdmin(), () -> {
            if (args.size() != 1) return "Error: invalid instruction";
            int id;
            try { id = parseIntArg(args.get(0)); }
            catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.deleteUser(id));
        });
    }

    private String handleQueryUser(List<String> args) {
        return requirePermission(isAdmin(), () -> {
            if (args.size() != 1) return "Error: invalid instruction";
            int id;
            try { id = parseIntArg(args.get(0)); }
            catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.queryUser(id));
        });
    }

    // ── buildings ──

    private String handleBuilding(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 1) return "Error: invalid instruction";
            return respond(service.createBuilding(args.get(0)));
        });
    }

    private String handleModifyBuilding(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 2) return "Error: invalid instruction";
            return respond(service.modifyBuilding(args.get(0), args.get(1)));
        });
    }

    private String handleDeleteBuilding(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 1) return "Error: invalid instruction";
            return respond(service.deleteBuilding(args.get(0)));
        });
    }

    private String handleQueryBuilding(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 1) return "Error: invalid instruction";
            return respond(service.queryBuilding(args.get(0)));
        });
    }

    // ── classrooms ──

    private String handleClassroom(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 3) return "Error: invalid instruction";
            long number, capacity;
            try {
                number = parseNumber(args.get(1));
                capacity = parseNumber(args.get(2));
            } catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.createClassroom(args.get(0), number, (int) capacity));
        });
    }

    private String handleModifyClassroom(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 3) return "Error: invalid instruction";
            long number, capacity;
            try {
                number = parseNumber(args.get(1));
                capacity = parseNumber(args.get(2));
            } catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.modifyClassroom(args.get(0), number, (int) capacity));
        });
    }

    private String handleDeleteClassroom(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 2) return "Error: invalid instruction";
            long number;
            try { number = parseNumber(args.get(1)); }
            catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.deleteClassroom(args.get(0), number));
        });
    }

    // ── courses ──

    private String handleCourse(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 4) return "Error: invalid instruction";
            long code;
            int semester, credits;
            try {
                code = parseNumber(args.get(0));
                semester = parseIntArg(args.get(2));
                credits = parseIntArg(args.get(3));
            } catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.createCourse(code, args.get(1), semester, credits));
        });
    }

    private String handleModifyCourse(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 4) return "Error: invalid instruction";
            long code;
            int semester, credits;
            try {
                code = parseNumber(args.get(0));
                semester = parseIntArg(args.get(2));
                credits = parseIntArg(args.get(3));
            } catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.modifyCourse(code, args.get(1), semester, credits));
        });
    }

    private String handleDeleteCourse(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 1) return "Error: invalid instruction";
            long code;
            try { code = parseNumber(args.get(0)); }
            catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.deleteCourse(code));
        });
    }

    private String handleQueryCourse(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 1) return "Error: invalid instruction";
            long code;
            try { code = parseNumber(args.get(0)); }
            catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.queryCourse(code));
        });
    }

    // ── students ──

    private String handleStudent(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 3) return "Error: invalid instruction";
            long number;
            try { number = parseNumber(args.get(0)); }
            catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.createStudent(number, args.get(1), args.get(2)));
        });
    }

    private String handleModifyStudent(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 3) return "Error: invalid instruction";
            long number;
            try { number = parseNumber(args.get(0)); }
            catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.modifyStudent(number, args.get(1), args.get(2)));
        });
    }

    private String handleDeleteStudent(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 1) return "Error: invalid instruction";
            long number;
            try { number = parseNumber(args.get(0)); }
            catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.deleteStudent(number));
        });
    }

    private String handleQueryStudent(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 1) return "Error: invalid instruction";
            long number;
            try { number = parseNumber(args.get(0)); }
            catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.queryStudent(number));
        });
    }

    // ── professors ──

    private String handleProfessor(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 3) return "Error: invalid instruction";
            long id;
            try { id = parseNumber(args.get(0)); }
            catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.createProfessor(id, args.get(1), args.get(2)));
        });
    }

    private String handleModifyProfessor(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 3) return "Error: invalid instruction";
            long id;
            try { id = parseNumber(args.get(0)); }
            catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.modifyProfessor(id, args.get(1), args.get(2)));
        });
    }

    private String handleDeleteProfessor(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 1) return "Error: invalid instruction";
            long id;
            try { id = parseNumber(args.get(0)); }
            catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.deleteProfessor(id));
        });
    }

    private String handleQueryProfessor(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 1) return "Error: invalid instruction";
            long id;
            try { id = parseNumber(args.get(0)); }
            catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.queryProfessor(id));
        });
    }

    // ── sections ──

    private String handleSection(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 7) return "Error: invalid instruction";
            long code, classroomNumber, professorId, courseCode;
            try {
                code = parseNumber(args.get(0));
                courseCode = parseNumber(args.get(3));
                classroomNumber = parseNumber(args.get(4));
                professorId = parseNumber(args.get(6));
            } catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.createSection(code, args.get(1), args.get(2),
                    courseCode, classroomNumber, args.get(5), professorId));
        });
    }

    private String handleModifySection(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 7) return "Error: invalid instruction";
            long code, courseCode, classroomNumber, professorId;
            try {
                code = parseNumber(args.get(0));
                courseCode = parseNumber(args.get(3));
                classroomNumber = parseNumber(args.get(4));
                professorId = parseNumber(args.get(6));
            } catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.modifySection(code, args.get(1), args.get(2),
                    courseCode, classroomNumber, args.get(5), professorId));
        });
    }

    private String handleDeleteSection(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 1) return "Error: invalid instruction";
            long code;
            try { code = parseNumber(args.get(0)); }
            catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.deleteSection(code));
        });
    }

    private String handleQuerySection(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 1) return "Error: invalid instruction";
            long code;
            try { code = parseNumber(args.get(0)); }
            catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.querySection(code));
        });
    }

    // ── enrollments ──

    private String handleEnroll(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 4) return "Error: invalid instruction";
            long studentNumber, courseCode;
            int coursework, finalExam;
            try {
                studentNumber = parseNumber(args.get(0));
                courseCode = parseNumber(args.get(1));
                coursework = parseIntArg(args.get(2));
                finalExam = parseIntArg(args.get(3));
            } catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.enroll(studentNumber, courseCode, coursework, finalExam));
        });
    }

    private String handleEnrollMe(List<String> args) {
        return requirePermission(isStudent(), () -> {
            if (args.size() != 1) return "Error: invalid instruction";
            long courseCode;
            try { courseCode = parseNumber(args.get(0)); }
            catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.enrollMe(courseCode));
        });
    }

    private String handleChangeGrades(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 4) return "Error: invalid instruction";
            long studentNumber, courseCode;
            int coursework, finalExam;
            try {
                studentNumber = parseNumber(args.get(0));
                courseCode = parseNumber(args.get(1));
                coursework = parseIntArg(args.get(2));
                finalExam = parseIntArg(args.get(3));
            } catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.changeGrades(studentNumber, courseCode, coursework, finalExam));
        });
    }

    private String handleUnenroll(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 2) return "Error: invalid instruction";
            long studentNumber, courseCode;
            try {
                studentNumber = parseNumber(args.get(0));
                courseCode = parseNumber(args.get(1));
            } catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return respond(service.unenroll(studentNumber, courseCode));
        });
    }

    // ── load file ──

    private static final java.util.Set<String> LOAD_ALLOWED = java.util.Set.of(
            "user", "building", "classroom", "course", "student", "professor", "section", "enroll");

    private String handleLoad(List<String> args) {
        return requirePermission(isAdmin(), () -> {
            if (args.size() != 1) return "Error: invalid instruction";
            String path = args.get(0);
            List<String> lines;
            try {
                lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
            } catch (IOException e) {
                return "Error: " + e.getMessage();
            }
            int correct = 0, errors = 0;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                int lineNum = i + 1;
                // empty lines count neither as correct nor as errors
                if (line.isBlank()) continue;
                // parse
                Parser.Instruction instr;
                try {
                    instr = Parser.parse(line);
                } catch (Parser.ParseException ex) {
                    errors++;
                    sb.append("Error in line ").append(lineNum).append(": invalid instruction\n");
                    continue;
                }
                if (instr == null) continue; // blank (already handled above, but defensive)
                if (!LOAD_ALLOWED.contains(instr.name())) {
                    errors++;
                    sb.append("Error in line ").append(lineNum).append(": invalid instruction\n");
                    continue;
                }
                String result = dispatchLoadLine(instr.name(), instr.args());
                if (result.startsWith("Error:") || result.startsWith("Error ")) {
                    errors++;
                    // extract the reason after "Error: "
                    String reason = result.startsWith("Error: ") ? result.substring(7) : result.substring(6);
                    sb.append("Error in line ").append(lineNum).append(": ").append(reason).append("\n");
                } else {
                    correct++;
                }
            }
            sb.append("OK: load finished, ").append(correct).append(" correct and ").append(errors).append(" with errors");
            return sb.toString();
        });
    }

    /**
     * Dispatch one of the allowed load instructions.
     * Returns the response string (starting with "OK:" or "Error:").
     */
    private String dispatchLoadLine(String name, List<String> args) {
        return switch (name) {
            case "user"       -> handleUser(args);
            case "building"   -> handleBuilding(args);
            case "classroom"  -> handleClassroom(args);
            case "course"     -> handleCourse(args);
            case "student"    -> handleStudent(args);
            case "professor"  -> handleProfessor(args);
            case "section"    -> handleSection(args);
            case "enroll"     -> handleEnroll(args);
            default -> "Error: invalid instruction";
        };
    }

    // ── reports ──

    private String handleStudentCourses(List<String> args) {
        if (args.size() != 1) return "Error: invalid instruction";
        long studentNumber;
        try { studentNumber = parseNumber(args.get(0)); }
        catch (Parser.ParseException e) { return "Error: invalid instruction"; }
        // admin and staff may query any student; student user may only query their own
        Long requestingStudentNumber = null;
        if (isStudent()) {
            if (service.currentUser().getStudentNumber() == null)
                return "Error: permission denied";
            requestingStudentNumber = service.currentUser().getStudentNumber();
        } else if (!isAdminOrStaff()) {
            return "Error: permission denied";
        }
        return reportRespond(service.studentCourses(studentNumber, requestingStudentNumber));
    }

    private String handleCourseStudents(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 1) return "Error: invalid instruction";
            long courseCode;
            try { courseCode = parseNumber(args.get(0)); }
            catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return reportRespond(service.courseStudents(courseCode));
        });
    }

    private String handleClassroomSections(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 2) return "Error: invalid instruction";
            long number;
            try { number = parseNumber(args.get(1)); }
            catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return reportRespond(service.classroomSections(args.get(0), number));
        });
    }

    private String handlePassedBySemester(List<String> args) {
        return requirePermission(isAdminOrStaff(), () -> {
            if (args.size() != 1) return "Error: invalid instruction";
            int semester;
            try { semester = parseIntArg(args.get(0)); }
            catch (Parser.ParseException e) { return "Error: invalid instruction"; }
            return reportRespond(service.passedBySemester(semester));
        });
    }

    /** For reports: success returns the message directly (no "OK:" prefix); errors use "Error:". */
    private static String reportRespond(Result r) {
        return r.ok() ? r.message() : "Error: " + r.message();
    }
}
