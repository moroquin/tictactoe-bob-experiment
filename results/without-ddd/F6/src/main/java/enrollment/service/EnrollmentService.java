package enrollment.service;

import enrollment.model.*;
import enrollment.store.Store;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * All business rules live here. The console calls these methods; it does not
 * make domain decisions.
 *
 * Session state is managed here (R6).
 */
public class EnrollmentService {

    private final Store store;
    private User currentUser = null; // null means no session

    public EnrollmentService(Store store) {
        this.store = store;
    }

    // ────────────────────────────────────────────────────────── session ──

    public boolean hasSession() { return currentUser != null; }
    public User currentUser() { return currentUser; }

    /** R6, R12: login */
    public Result login(String name, String password) {
        if (hasSession()) return Result.error("a session is already open");
        User u = store.getUserByName(name);
        if (u == null || !u.getPassword().equals(password)) {
            return Result.error("wrong name or password");
        }
        currentUser = u;
        return Result.ok("logged in as " + name);
    }

    /** Logout */
    public Result logout() {
        currentUser = null;
        return Result.ok("logged out");
    }

    // ────────────────────────────────────────────────────── users ──

    /** R11, R12, R13: create user */
    public Result createUser(String name, String password, UserType type, Long studentNumber) {
        if (store.getUserByName(name) != null)
            return Result.error("user name " + name + " already exists");
        if (type == UserType.STUDENT) {
            if (studentNumber == null)
                return Result.error("student number required for student type");
            Student s = store.getStudentByNumber(studentNumber);
            if (s == null)
                return Result.error("student " + studentNumber + " does not exist");
            // each student has at most one user
            boolean taken = store.allUsers().stream()
                    .anyMatch(u -> u.getType() == UserType.STUDENT
                            && studentNumber.equals(u.getStudentNumber()));
            if (taken)
                return Result.error("student " + studentNumber + " already has a user");
        } else {
            if (studentNumber != null)
                return Result.error("student number not allowed for non-student type");
        }
        int id = store.addUser(name, password, type, studentNumber);
        return Result.ok("user created with ID " + id);
    }

    /** R15, R12, R13: modify user */
    public Result modifyUser(int id, String name, String password, UserType type, Long studentNumber) {
        User u = store.getUserById(id);
        if (u == null) return Result.error("user " + id + " does not exist");
        // R5: admin user type cannot change
        if (id == 1 && type != UserType.ADMIN)
            return Result.error("cannot change type of admin user");
        // name uniqueness (excluding self)
        User other = store.getUserByName(name);
        if (other != null && other.getId() != id)
            return Result.error("user name " + name + " already exists");
        if (type == UserType.STUDENT) {
            if (studentNumber == null)
                return Result.error("student number required for student type");
            Student s = store.getStudentByNumber(studentNumber);
            if (s == null)
                return Result.error("student " + studentNumber + " does not exist");
            boolean taken = store.allUsers().stream()
                    .anyMatch(u2 -> u2.getId() != id
                            && u2.getType() == UserType.STUDENT
                            && studentNumber.equals(u2.getStudentNumber()));
            if (taken)
                return Result.error("student " + studentNumber + " already has a user");
        } else {
            if (studentNumber != null)
                return Result.error("student number not allowed for non-student type");
        }
        u.setName(name);
        u.setPassword(password);
        u.setType(type);
        u.setStudentNumber(studentNumber);
        return Result.ok("user " + id + " modified");
    }

    /** R14, R5: delete user */
    public Result deleteUser(int id) {
        User u = store.getUserById(id);
        if (u == null) return Result.error("user " + id + " does not exist");
        if (id == 1) return Result.error("cannot delete admin user");
        if (currentUser != null && currentUser.getId() == id)
            return Result.error("cannot delete the user with the open session");
        store.removeUser(id);
        return Result.ok("user " + id + " deleted");
    }

    /** QueryUser */
    public Result queryUser(int id) {
        User u = store.getUserById(id);
        if (u == null) return Result.error("user " + id + " does not exist");
        // format: id | name | type [| studentNumber]
        String line = u.getId() + " | " + u.getName() + " | " + u.getType();
        if (u.getType() == UserType.STUDENT && u.getStudentNumber() != null)
            line += " | " + u.getStudentNumber();
        return Result.ok(line);
    }

    // ──────────────────────────────────────────────── buildings ──

    /** R9: create building */
    public Result createBuilding(String name) {
        if (store.getBuildingByName(name) != null)
            return Result.error("building " + name + " already exists");
        store.addBuilding(name);
        return Result.ok("building " + name + " created");
    }

    /** R18: modify building */
    public Result modifyBuilding(String name, String newName) {
        Building b = store.getBuildingByName(name);
        if (b == null) return Result.error("building " + name + " does not exist");
        if (!name.equals(newName) && store.getBuildingByName(newName) != null)
            return Result.error("building " + newName + " already exists");
        store.renameBuilding(b, newName);
        return Result.ok("building renamed to " + newName);
    }

    /** R19: delete building */
    public Result deleteBuilding(String name) {
        Building b = store.getBuildingByName(name);
        if (b == null) return Result.error("building " + name + " does not exist");
        if (!b.getClassrooms().isEmpty())
            return Result.error("building " + name + " has classrooms");
        store.removeBuilding(name);
        return Result.ok("building " + name + " deleted");
    }

    /** QueryBuilding */
    public Result queryBuilding(String name) {
        Building b = store.getBuildingByName(name);
        if (b == null) return Result.error("building " + name + " does not exist");
        if (b.getClassrooms().isEmpty()) {
            return Result.ok(name + " | no classrooms");
        }
        String rooms = b.getClassrooms().stream()
                .sorted(Comparator.comparingLong(Classroom::getNumber))
                .map(c -> c.getNumber() + " (" + c.getCapacity() + ")")
                .collect(Collectors.joining(", "));
        return Result.ok(name + " | " + rooms);
    }

    // ──────────────────────────────────────────────── classrooms ──

    /** R9, R16: create classroom */
    public Result createClassroom(String buildingName, long number, int capacity) {
        Building b = store.getBuildingByName(buildingName);
        if (b == null) return Result.error("building " + buildingName + " does not exist");
        if (capacity <= 0)
            return Result.error("capacity must be greater than 0");
        if (store.getClassroom(b, number) != null)
            return Result.error("classroom " + number + " already exists in building " + buildingName);
        store.addClassroom(b, number, capacity);
        return Result.ok("classroom " + number + " created in building " + buildingName);
    }

    /** R17: modify classroom */
    public Result modifyClassroom(String buildingName, long number, int capacity) {
        Building b = store.getBuildingByName(buildingName);
        if (b == null) return Result.error("building " + buildingName + " does not exist");
        Classroom c = store.getClassroom(b, number);
        if (c == null) return Result.error("classroom " + number + " does not exist in building " + buildingName);
        if (capacity <= 0)
            return Result.error("capacity must be greater than 0");
        // capacity cannot fall below number of students enrolled in any of its sections
        int maxEnrolled = store.allSections().stream()
                .filter(s -> s.getClassroom() == c)
                .mapToInt(Section::enrolledCount)
                .max().orElse(0);
        if (capacity < maxEnrolled)
            return Result.error("capacity cannot be less than " + maxEnrolled + " (students enrolled)");
        c.setCapacity(capacity);
        return Result.ok("classroom " + number + " modified");
    }

    /** R19: delete classroom */
    public Result deleteClassroom(String buildingName, long number) {
        Building b = store.getBuildingByName(buildingName);
        if (b == null) return Result.error("building " + buildingName + " does not exist");
        Classroom c = store.getClassroom(b, number);
        if (c == null) return Result.error("classroom " + number + " does not exist in building " + buildingName);
        boolean usedBySection = store.allSections().stream()
                .anyMatch(s -> s.getClassroom() == c);
        if (usedBySection)
            return Result.error("classroom " + number + " is used by a section");
        store.removeClassroom(b, number);
        return Result.ok("classroom " + number + " deleted from building " + buildingName);
    }

    // ──────────────────────────────────────────────── courses ──

    /** R9, R20: create course */
    public Result createCourse(long code, String name, int semester, int credits) {
        if (store.getCourseByCode(code) != null)
            return Result.error("course " + code + " already exists");
        if (semester < 1 || semester > 10)
            return Result.error("semester must be between 1 and 10");
        if (credits <= 0)
            return Result.error("credits must be greater than 0");
        store.addCourse(code, name, semester, credits);
        return Result.ok("course " + code + " created");
    }

    /** R20: modify course — CR-5: cannot raise credits if any enrolled student would exceed 24 */
    public Result modifyCourse(long code, String name, int semester, int credits) {
        Course c = store.getCourseByCode(code);
        if (c == null) return Result.error("course " + code + " does not exist");
        if (semester < 1 || semester > 10)
            return Result.error("semester must be between 1 and 10");
        if (credits <= 0)
            return Result.error("credits must be greater than 0");
        // CR-5: if credits would increase, check no enrolled student would exceed 24
        int oldCredits = c.getCredits();
        if (credits > oldCredits) {
            int delta = credits - oldCredits;
            // find all students enrolled in this course
            boolean wouldExceed = store.allSections().stream()
                    .filter(s -> s.getCourse().getCode() == code)
                    .flatMap(s -> s.getEnrollments().stream())
                    .anyMatch(e -> currentCredits(e.getStudent()) + delta > 24);
            if (wouldExceed)
                return Result.error("raising credits would put a student above 24 credits");
        }
        c.setName(name);
        c.setSemester(semester);
        c.setCredits(credits);
        return Result.ok("course " + code + " modified");
    }

    /** R21, CR-5: delete course — cannot delete if it is a prerequisite of another course */
    public Result deleteCourse(long code) {
        Course c = store.getCourseByCode(code);
        if (c == null) return Result.error("course " + code + " does not exist");
        boolean hasSection = store.allSections().stream()
                .anyMatch(s -> s.getCourse().getCode() == code);
        if (hasSection)
            return Result.error("course " + code + " has sections");
        // CR-5: cannot delete if it is a prerequisite of another course
        boolean isPrereq = store.allCourses().stream()
                .anyMatch(other -> other.getPrerequisites().stream()
                        .anyMatch(p -> p.getCode() == code));
        if (isPrereq)
            return Result.error("course " + code + " is a prerequisite of another course");
        // CR-5: deleting a course that has prerequisites (and no sections) removes its prerequisites too
        c.getPrerequisites().clear();
        store.removeCourse(code);
        return Result.ok("course " + code + " deleted");
    }

    /** QueryCourse — CR-5: append prerequisites line */
    public Result queryCourse(long code) {
        Course c = store.getCourseByCode(code);
        if (c == null) return Result.error("course " + code + " does not exist");
        String prereqStr;
        if (c.getPrerequisites().isEmpty()) {
            prereqStr = "none";
        } else {
            prereqStr = c.getPrerequisites().stream()
                    .map(p -> String.valueOf(p.getCode()))
                    .collect(Collectors.joining(", "));
        }
        return Result.ok(c.getCode() + " | " + c.getName() + " | " + c.getSemester()
                + " | " + c.getCredits() + " | prerequisites: " + prereqStr);
    }

    /** CR-5: add a prerequisite to a course */
    public Result addPrerequisite(long courseCode, long prerequisiteCode) {
        Course course = store.getCourseByCode(courseCode);
        if (course == null) return Result.error("course " + courseCode + " does not exist");
        Course prereq = store.getCourseByCode(prerequisiteCode);
        if (prereq == null) return Result.error("course " + prerequisiteCode + " does not exist");
        if (courseCode == prerequisiteCode)
            return Result.error("a course cannot require itself");
        // check pair does not already exist
        if (course.getPrerequisites().stream().anyMatch(p -> p.getCode() == prerequisiteCode))
            return Result.error("course " + courseCode + " already requires course " + prerequisiteCode);
        // check no cycle: would adding prereq as a prerequisite of course create a cycle?
        // i.e. is courseCode reachable from prereq?
        if (isReachable(prereq, courseCode))
            return Result.error("adding this prerequisite would create a cycle");
        course.getPrerequisites().add(prereq);
        return Result.ok("course " + prerequisiteCode + " is now a prerequisite of course " + courseCode);
    }

    /** Returns true if targetCode is reachable from start following prerequisites. */
    private boolean isReachable(Course start, long targetCode) {
        // BFS/DFS through prerequisites of start
        java.util.Set<Long> visited = new java.util.HashSet<>();
        java.util.Queue<Course> queue = new java.util.ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            Course cur = queue.poll();
            for (Course p : cur.getPrerequisites()) {
                if (p.getCode() == targetCode) return true;
                if (visited.add(p.getCode())) queue.add(p);
            }
        }
        return false;
    }

    // ──────────────────────────────────────────────── students ──

    /** R9: create student */
    public Result createStudent(long number, String name, String address) {
        if (store.getStudentByNumber(number) != null)
            return Result.error("student " + number + " already exists");
        store.addStudent(number, name, address);
        return Result.ok("student " + number + " created");
    }

    /** Modify student */
    public Result modifyStudent(long number, String name, String address) {
        Student s = store.getStudentByNumber(number);
        if (s == null) return Result.error("student " + number + " does not exist");
        s.setName(name);
        s.setAddress(address);
        return Result.ok("student " + number + " modified");
    }

    /** R22: delete student */
    public Result deleteStudent(long number) {
        Student s = store.getStudentByNumber(number);
        if (s == null) return Result.error("student " + number + " does not exist");
        // no enrollments
        boolean hasEnrollment = store.allSections().stream()
                .flatMap(sec -> sec.getEnrollments().stream())
                .anyMatch(e -> e.getStudent().getStudentNumber() == number);
        if (hasEnrollment)
            return Result.error("student " + number + " has enrollments");
        // no linked user
        boolean hasUser = store.allUsers().stream()
                .anyMatch(u -> u.getType() == UserType.STUDENT && number == u.getStudentNumber());
        if (hasUser)
            return Result.error("student " + number + " has a linked user");
        store.removeStudent(number);
        return Result.ok("student " + number + " deleted");
    }

    /** QueryStudent */
    public Result queryStudent(long number) {
        Student s = store.getStudentByNumber(number);
        if (s == null) return Result.error("student " + number + " does not exist");
        return Result.ok(s.getStudentNumber() + " | " + s.getName() + " | " + s.getAddress());
    }

    // ──────────────────────────────────────────────── professors ──

    /** R9: create professor */
    public Result createProfessor(long id, String name, String address) {
        if (store.getProfessorById(id) != null)
            return Result.error("professor " + id + " already exists");
        store.addProfessor(id, name, address);
        return Result.ok("professor " + id + " created");
    }

    /** Modify professor */
    public Result modifyProfessor(long id, String name, String address) {
        Professor p = store.getProfessorById(id);
        if (p == null) return Result.error("professor " + id + " does not exist");
        p.setName(name);
        p.setAddress(address);
        return Result.ok("professor " + id + " modified");
    }

    /** R23: delete professor */
    public Result deleteProfessor(long id) {
        Professor p = store.getProfessorById(id);
        if (p == null) return Result.error("professor " + id + " does not exist");
        boolean usedBySection = store.allSections().stream()
                .anyMatch(s -> s.getProfessor().getProfessorId() == id);
        if (usedBySection)
            return Result.error("professor " + id + " is used by a section");
        store.removeProfessor(id);
        return Result.ok("professor " + id + " deleted");
    }

    /** QueryProfessor */
    public Result queryProfessor(long id) {
        Professor p = store.getProfessorById(id);
        if (p == null) return Result.error("professor " + id + " does not exist");
        return Result.ok(p.getProfessorId() + " | " + p.getName() + " | " + p.getAddress());
    }

    // ──────────────────────────────────────────────── sections ──

    private static final Pattern PERIOD_RE =
            Pattern.compile("^(1[0-2]|[1-9]):[0-5][0-9](am|pm)-(1[0-2]|[1-9]):[0-5][0-9](am|pm)$",
                    Pattern.CASE_INSENSITIVE);

    private static final List<String> VALID_DAYS = List.of(
            "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday");

    /** R24, R25, R26, R27: create section */
    public Result createSection(long code, String period, String day, long courseCode,
                                long classroomNumber, String buildingName, long professorId) {
        if (store.getSectionByCode(code) != null)
            return Result.error("section " + code + " already exists");
        // validate period
        String periodLow = period.toLowerCase();
        if (!PERIOD_RE.matcher(periodLow).matches())
            return Result.error("invalid period: " + period);
        if (!isPeriodValid(periodLow))
            return Result.error("section start must be before end");
        // validate day
        String dayLow = day.toLowerCase();
        if (!VALID_DAYS.contains(dayLow))
            return Result.error("invalid day: " + day);
        // references
        Course course = store.getCourseByCode(courseCode);
        if (course == null) return Result.error("course " + courseCode + " does not exist");
        Building b = store.getBuildingByName(buildingName);
        if (b == null) return Result.error("building " + buildingName + " does not exist");
        Classroom classroom = store.getClassroom(b, classroomNumber);
        if (classroom == null) return Result.error("classroom " + classroomNumber + " does not exist in building " + buildingName);
        Professor professor = store.getProfessorById(professorId);
        if (professor == null) return Result.error("professor " + professorId + " does not exist");
        store.addSection(code, periodLow, dayLow, course, classroom, professor);
        return Result.ok("section " + code + " created");
    }

    /** QuerySection */
    public Result querySection(long code) {
        Section s = store.getSectionByCode(code);
        if (s == null) return Result.error("section " + code + " does not exist");
        // code | period | day | courseCode | classroomNumber | building | professorId | enrolled/capacity
        return Result.ok(s.getCode() + " | " + s.getPeriod() + " | " + s.getDay()
                + " | " + s.getCourse().getCode()
                + " | " + s.getClassroom().getNumber()
                + " | " + s.getClassroom().getBuilding().getName()
                + " | " + s.getProfessor().getProfessorId()
                + " | " + s.enrolledCount() + "/" + s.getClassroom().getCapacity());
    }

    /** CR-1: modify section (R24–R26; if enrollments exist, course cannot change and
     *  new classroom capacity must cover enrolled count). */
    public Result modifySection(long code, String period, String day, long courseCode,
                                long classroomNumber, String buildingName, long professorId) {
        Section s = store.getSectionByCode(code);
        if (s == null) return Result.error("section " + code + " does not exist");
        // validate period
        String periodLow = period.toLowerCase();
        if (!PERIOD_RE.matcher(periodLow).matches())
            return Result.error("invalid period: " + period);
        if (!isPeriodValid(periodLow))
            return Result.error("section start must be before end");
        // validate day
        String dayLow = day.toLowerCase();
        if (!VALID_DAYS.contains(dayLow))
            return Result.error("invalid day: " + day);
        // resolve references
        Course course = store.getCourseByCode(courseCode);
        if (course == null) return Result.error("course " + courseCode + " does not exist");
        Building b = store.getBuildingByName(buildingName);
        if (b == null) return Result.error("building " + buildingName + " does not exist");
        Classroom classroom = store.getClassroom(b, classroomNumber);
        if (classroom == null) return Result.error("classroom " + classroomNumber + " does not exist in building " + buildingName);
        Professor professor = store.getProfessorById(professorId);
        if (professor == null) return Result.error("professor " + professorId + " does not exist");
        // if section has enrollments: course cannot change; new classroom capacity must cover enrolled count
        int enrolled = s.enrolledCount();
        if (enrolled > 0) {
            if (course.getCode() != s.getCourse().getCode())
                return Result.error("cannot change course of section " + code + " because it has enrollments");
            if (classroom.getCapacity() < enrolled)
                return Result.error("new classroom capacity " + classroom.getCapacity()
                        + " is less than enrolled count " + enrolled);
        }
        s.setPeriod(periodLow);
        s.setDay(dayLow);
        s.setCourse(course);
        s.setClassroom(classroom);
        s.setProfessor(professor);
        return Result.ok("section " + code + " modified");
    }

    /** CR-1: delete section — only if it has no enrollments (R19, R21, R23 become applicable). */
    public Result deleteSection(long code) {
        Section s = store.getSectionByCode(code);
        if (s == null) return Result.error("section " + code + " does not exist");
        if (!s.getEnrollments().isEmpty())
            return Result.error("section " + code + " has enrollments");
        store.removeSection(code);
        return Result.ok("section " + code + " deleted");
    }

    // ──────────────────────────────────────────────── enrollments ──

    /** R28–R32: enroll by admin/staff */
    public Result enroll(long studentNumber, long courseCode, int coursework, int finalExam) {
        Student student = store.getStudentByNumber(studentNumber);
        if (student == null) return Result.error("student " + studentNumber + " does not exist");
        Course course = store.getCourseByCode(courseCode);
        if (course == null) return Result.error("course " + courseCode + " does not exist");
        // R28: validate grades
        if (coursework < 0 || coursework > 70)
            return Result.error("coursework must be between 0 and 70");
        if (finalExam < 0 || finalExam > 30)
            return Result.error("final exam must be between 0 and 30");
        return doEnroll(student, course, coursework, finalExam);
    }

    /** R31: enroll self (student user) */
    public Result enrollMe(long courseCode) {
        if (currentUser == null || currentUser.getType() != UserType.STUDENT)
            return Result.error("not a student user");
        Long studentNumber = currentUser.getStudentNumber();
        if (studentNumber == null)
            return Result.error("no student linked to this user");
        Student student = store.getStudentByNumber(studentNumber);
        if (student == null) return Result.error("student " + studentNumber + " does not exist");
        Course course = store.getCourseByCode(courseCode);
        if (course == null) return Result.error("course " + courseCode + " does not exist");
        return doEnroll(student, course, 0, 0);
    }

    private Result doEnroll(Student student, Course course, int coursework, int finalExam) {
        long studentNumber = student.getStudentNumber();
        long courseCode = course.getCode();
        // R30: already enrolled?
        if (store.findEnrollment(studentNumber, courseCode) != null)
            return Result.error("student " + studentNumber + " is already enrolled in course " + courseCode);
        // CR-5: check prerequisites — student must have passed every prerequisite
        for (Course prereq : course.getPrerequisites()) {
            Enrollment prereqEnrollment = store.findEnrollment(studentNumber, prereq.getCode());
            if (prereqEnrollment == null || !prereqEnrollment.passes())
                return Result.error("student " + studentNumber + " has not passed prerequisite course " + prereq.getCode());
        }
        // CR-5: credit limit — enrolling must not take student above 24 credits
        if (currentCredits(student) + course.getCredits() > 24)
            return Result.error("student " + studentNumber + " would exceed the 24-credit limit");
        // CR-3: find section with fewest enrollments that has room; ties broken by lowest code
        Section target = store.allSections().stream()
                .filter(s -> s.getCourse().getCode() == courseCode && s.hasRoom())
                .min(Comparator.comparingInt(Section::enrolledCount)
                        .thenComparingLong(Section::getCode))
                .orElse(null);
        if (target == null)
            return Result.error("no section with room in course " + courseCode);
        target.getEnrollments().add(new Enrollment(student, target, coursework, finalExam));
        return Result.ok(studentNumber + " enrolled in course " + courseCode + " in section " + target.getCode());
    }

    /** Returns the total credits of all courses a student is currently enrolled in. */
    private int currentCredits(Student student) {
        long studentNumber = student.getStudentNumber();
        return store.allSections().stream()
                .flatMap(s -> s.getEnrollments().stream())
                .filter(e -> e.getStudent().getStudentNumber() == studentNumber)
                .mapToInt(e -> e.getSection().getCourse().getCredits())
                .sum();
    }

    /** R32: change grades */
    public Result changeGrades(long studentNumber, long courseCode, int coursework, int finalExam) {
        Enrollment e = store.findEnrollment(studentNumber, courseCode);
        if (e == null)
            return Result.error("student " + studentNumber + " is not enrolled in course " + courseCode);
        if (coursework < 0 || coursework > 70)
            return Result.error("coursework must be between 0 and 70");
        if (finalExam < 0 || finalExam > 30)
            return Result.error("final exam must be between 0 and 30");
        e.setCoursework(coursework);
        e.setFinalExam(finalExam);
        return Result.ok("grades updated for student " + studentNumber + " in course " + courseCode);
    }

    /** R32: unenroll */
    public Result unenroll(long studentNumber, long courseCode) {
        Enrollment e = store.findEnrollment(studentNumber, courseCode);
        if (e == null)
            return Result.error("student " + studentNumber + " is not enrolled in course " + courseCode);
        e.getSection().getEnrollments().remove(e);
        return Result.ok("student " + studentNumber + " unenrolled from course " + courseCode);
    }

    // ──────────────────────────────────────────────── helpers ──

    /**
     * R24: parse and compare times to check start < end.
     * Period has already been validated by regex.
     */
    private static boolean isPeriodValid(String period) {
        int hyphen = period.indexOf('-');
        String start = period.substring(0, hyphen);
        String end = period.substring(hyphen + 1);
        return toMinutes(start) < toMinutes(end);
    }

    /** Convert "h:mmam" or "h:mmpm" to minutes since midnight. */
    private static int toMinutes(String time) {
        boolean pm = time.endsWith("pm");
        String t = time.substring(0, time.length() - 2); // strip am/pm
        int colon = t.indexOf(':');
        int h = Integer.parseInt(t.substring(0, colon));
        int m = Integer.parseInt(t.substring(colon + 1));
        // 12-hour to 24-hour
        if (pm && h != 12) h += 12;
        if (!pm && h == 12) h = 0;
        return h * 60 + m;
    }

    // ──────────────────────────────────────────────── CR-2 reports ──

    /**
     * StudentCourses(studentNumber): enrollments of the given student, in ascending section code.
     * Permission: admin, staff, or the student's own user.
     * @param requestingStudentNumber null means admin/staff; non-null means student user requesting own.
     */
    public Result studentCourses(long studentNumber, Long requestingStudentNumber) {
        // permission: if a student number is passed and it differs, deny
        if (requestingStudentNumber != null && requestingStudentNumber != studentNumber)
            return Result.error("permission denied");
        Student s = store.getStudentByNumber(studentNumber);
        if (s == null) return Result.error("student " + studentNumber + " does not exist");
        List<Enrollment> enrollments = store.allSections().stream()
                .sorted(Comparator.comparingLong(Section::getCode))
                .flatMap(sec -> sec.getEnrollments().stream()
                        .filter(e -> e.getStudent().getStudentNumber() == studentNumber))
                .collect(Collectors.toList());
        if (enrollments.isEmpty()) return Result.ok("No courses");
        String lines = enrollments.stream()
                .map(e -> {
                    Course c = e.getSection().getCourse();
                    String passedStr = e.passes() ? "passed" : "failed";
                    return c.getCode() + " | " + c.getName()
                            + " | section " + e.getSection().getCode()
                            + " | coursework " + e.getCoursework()
                            + " | final " + e.getFinalExam()
                            + " | " + passedStr;
                })
                .collect(Collectors.joining("\n"));
        return Result.ok(lines);
    }

    /**
     * CourseStudents(courseCode): enrollments in the course, in ascending section code,
     * within a section in enrollment order.
     */
    public Result courseStudents(long courseCode) {
        Course c = store.getCourseByCode(courseCode);
        if (c == null) return Result.error("course " + courseCode + " does not exist");
        // sections of this course, sorted by code
        List<Section> sections = store.allSections().stream()
                .filter(s -> s.getCourse().getCode() == courseCode)
                .sorted(Comparator.comparingLong(Section::getCode))
                .collect(Collectors.toList());
        List<String> lines = new ArrayList<>();
        for (Section sec : sections) {
            for (Enrollment e : sec.getEnrollments()) {
                Student st = e.getStudent();
                String passedStr = e.passes() ? "passed" : "failed";
                lines.add(st.getStudentNumber() + " | " + st.getName()
                        + " | section " + sec.getCode()
                        + " | coursework " + e.getCoursework()
                        + " | final " + e.getFinalExam()
                        + " | " + passedStr);
            }
        }
        if (lines.isEmpty()) return Result.ok("No students");
        return Result.ok(String.join("\n", lines));
    }

    /**
     * ClassroomSections(building, number): sections that use the classroom, in ascending section code.
     */
    public Result classroomSections(String buildingName, long number) {
        Building b = store.getBuildingByName(buildingName);
        if (b == null) return Result.error("building " + buildingName + " does not exist");
        Classroom classroom = store.getClassroom(b, number);
        if (classroom == null)
            return Result.error("classroom " + number + " does not exist in building " + buildingName);
        List<String> lines = store.allSections().stream()
                .filter(s -> s.getClassroom() == classroom)
                .sorted(Comparator.comparingLong(Section::getCode))
                .map(s -> "section " + s.getCode()
                        + " | " + s.getDay()
                        + " | " + s.getPeriod()
                        + " | " + s.getCourse().getCode()
                        + " | " + s.getCourse().getName())
                .collect(Collectors.toList());
        if (lines.isEmpty()) return Result.ok("No sections");
        return Result.ok(String.join("\n", lines));
    }

    /**
     * PassedBySemester(semester): one line per course of the semester (creation order),
     * then a totals line.
     */
    public Result passedBySemester(int semester) {
        if (semester < 1 || semester > 10)
            return Result.error("semester must be between 1 and 10");
        List<Course> courses = store.allCourses().stream()
                .filter(c -> c.getSemester() == semester)
                .collect(Collectors.toList()); // insertion order preserved
        if (courses.isEmpty()) return Result.ok("No courses in the semester");
        int totalPassed = 0, totalFailed = 0;
        List<String> lines = new ArrayList<>();
        for (Course c : courses) {
            long courseCode = c.getCode();
            List<Enrollment> enrollments = store.allSections().stream()
                    .filter(s -> s.getCourse().getCode() == courseCode)
                    .flatMap(s -> s.getEnrollments().stream())
                    .collect(Collectors.toList());
            int passed = (int) enrollments.stream().filter(Enrollment::passes).count();
            int failed = (int) enrollments.stream().filter(e -> !e.passes()).count();
            totalPassed += passed;
            totalFailed += failed;
            lines.add(c.getCode() + " | " + c.getName() + " | passed " + passed + " | failed " + failed);
        }
        lines.add("Total | passed " + totalPassed + " | failed " + totalFailed);
        return Result.ok(String.join("\n", lines));
    }
}
