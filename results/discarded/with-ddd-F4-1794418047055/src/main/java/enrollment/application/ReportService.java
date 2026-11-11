package enrollment.application;

import enrollment.domain.RuleViolation;
import enrollment.domain.access.Action;
import enrollment.domain.campus.Building;
import enrollment.domain.campus.ClassroomRef;
import enrollment.domain.catalog.Course;
import enrollment.domain.catalog.CourseCode;
import enrollment.domain.catalog.Semester;
import enrollment.domain.people.Student;
import enrollment.domain.people.StudentNumber;
import enrollment.domain.sections.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Application service for the four reports (I25–I28, CR-2).
 * None of these methods change any aggregate.
 */
public class ReportService {

    /**
     * One row of the StudentCourses report (I25).
     * courseCode | course name | section code | coursework | finalExam | passed
     */
    public record StudentCourseRow(
            long courseCode, String courseName,
            long sectionCode, int coursework, int finalExam, boolean passed) {}

    /**
     * One row of the CourseStudents report (I26).
     * studentNumber | name | section code | coursework | finalExam | passed
     */
    public record CourseStudentRow(
            long studentNumber, String studentName,
            long sectionCode, int coursework, int finalExam, boolean passed) {}

    /**
     * One row of the ClassroomSections report (I27).
     * section code | day | period | courseCode | course name
     */
    public record ClassroomSectionRow(
            long sectionCode, String day, String period,
            long courseCode, String courseName) {}

    /**
     * One row of the PassedBySemester report (I28).
     * courseCode | course name | passed | failed
     */
    public record SemesterCourseRow(
            long courseCode, String courseName, int passed, int failed) {}

    // ── Dependencies ──────────────────────────────────────────────────────────

    private final AccessService access;
    private final CampusService campus;
    private final CatalogService catalog;
    private final PeopleService people;
    private final SectionRepository sections;
    private final GradeScale gradeScale;

    public ReportService(AccessService access, CampusService campus,
                         CatalogService catalog, PeopleService people,
                         SectionRepository sections, GradeScale gradeScale) {
        this.access = access;
        this.campus = campus;
        this.catalog = catalog;
        this.people = people;
        this.sections = sections;
        this.gradeScale = gradeScale;
    }

    // ── StudentCourses (I25) ───────────────────────────────────────────────────

    /**
     * Courses a student is enrolled in, in ascending section code (I25).
     * A student may only query their own student number.
     */
    public List<StudentCourseRow> studentCourses(long studentNumber) {
        access.requireSession();
        access.requirePermission(Action.QUERY_ENROLLMENTS);
        // Students may only query their own number (I25)
        var user = access.session().user().orElseThrow(() -> new RuleViolation("log in first"));
        if (user.type() == enrollment.domain.access.UserType.STUDENT) {
            StudentNumber own = user.studentNumber()
                    .orElseThrow(() -> new RuleViolation("user is not linked to a student"));
            if (own.value() != studentNumber)
                throw new RuleViolation("permission denied");
        }
        // Validate student exists
        people.requireStudent(studentNumber);
        StudentNumber sn = new StudentNumber(studentNumber);
        // Collect from all sections
        List<Section> allSections = sections.listAll();
        List<StudentCourseRow> rows = new ArrayList<>();
        for (Section section : allSections) {
            for (Enrollment e : section.enrollments()) {
                if (e.student().equals(sn)) {
                    Course course = catalog.requireCourse(section.course().value());
                    rows.add(new StudentCourseRow(
                            course.code().value(), course.name(),
                            section.code().value(),
                            e.grades().coursework(), e.grades().finalExam(),
                            gradeScale.passed(e.grades())));
                }
            }
        }
        rows.sort(Comparator.comparingLong(StudentCourseRow::sectionCode));
        return rows;
    }

    // ── CourseStudents (I26) ───────────────────────────────────────────────────

    /**
     * Students enrolled in a course, in ascending section code, then enrollment order (I26).
     */
    public List<CourseStudentRow> courseStudents(long courseCode) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_ENROLLMENTS);
        // Validate course exists
        Course course = catalog.requireCourse(courseCode);
        CourseCode cc = new CourseCode(courseCode);
        List<Section> courseSections = sections.ofCourse(cc);
        // Already in ascending code order per SectionRepository contract
        List<CourseStudentRow> rows = new ArrayList<>();
        for (Section section : courseSections) {
            // Enrollments in enrollment order
            for (Enrollment e : section.enrollments()) {
                Student student = people.requireStudent(e.student().value());
                rows.add(new CourseStudentRow(
                        student.number().value(), student.name(),
                        section.code().value(),
                        e.grades().coursework(), e.grades().finalExam(),
                        gradeScale.passed(e.grades())));
            }
        }
        return rows;
    }

    // ── ClassroomSections (I27) ────────────────────────────────────────────────

    /**
     * Sections that use a classroom, in ascending section code (I27).
     */
    public List<ClassroomSectionRow> classroomSections(String buildingName, int classroomNumber) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SECTIONS);
        // Validate building and classroom exist
        Building b = campus.requireBuildingByName(buildingName);
        if (b.findClassroom(classroomNumber) == null)
            throw new RuleViolation("classroom " + classroomNumber + " does not exist in building " + buildingName);
        ClassroomRef ref = new ClassroomRef(b.id(), classroomNumber);
        List<Section> usingSections = sections.usingClassroom(ref);
        // Sort by ascending section code
        List<Section> sorted = usingSections.stream()
                .sorted(Comparator.comparingLong(s -> s.code().value()))
                .toList();
        List<ClassroomSectionRow> rows = new ArrayList<>();
        for (Section s : sorted) {
            Course course = catalog.requireCourse(s.course().value());
            rows.add(new ClassroomSectionRow(
                    s.code().value(), s.day().display(), s.period().toString(),
                    course.code().value(), course.name()));
        }
        return rows;
    }

    // ── PassedBySemester (I28) ────────────────────────────────────────────────

    /**
     * One row per course of the given semester, in creation order (I28).
     */
    public List<SemesterCourseRow> passedBySemester(int semester) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_ENROLLMENTS);
        // Validate semester range
        new Semester(semester); // throws RuleViolation if out of range
        // All courses in creation order
        List<Course> allCourses = catalog.listCourses();
        List<SemesterCourseRow> rows = new ArrayList<>();
        for (Course course : allCourses) {
            if (course.semester().value() != semester) continue;
            CourseCode cc = course.code();
            List<Section> courseSections = sections.ofCourse(cc);
            int passed = 0, failed = 0;
            for (Section section : courseSections) {
                for (Enrollment e : section.enrollments()) {
                    if (gradeScale.passed(e.grades())) passed++;
                    else failed++;
                }
            }
            rows.add(new SemesterCourseRow(course.code().value(), course.name(), passed, failed));
        }
        return rows;
    }
}
