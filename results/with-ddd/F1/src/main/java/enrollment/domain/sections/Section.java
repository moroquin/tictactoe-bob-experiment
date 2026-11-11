package enrollment.domain.sections;

import enrollment.domain.RuleViolation;
import enrollment.domain.campus.ClassroomRef;
import enrollment.domain.catalog.CourseCode;
import enrollment.domain.people.ProfessorId;
import enrollment.domain.people.StudentNumber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Section aggregate root: one offering of a Course.
 * Holds its Enrollments; protects capacity limit (I18).
 */
public class Section {

    private final SectionCode code;
    private final Period period;
    private final Day day;
    private final CourseCode course;
    private final ClassroomRef classroom;
    private final ProfessorId professor;
    private final List<Enrollment> enrollments = new ArrayList<>();

    public Section(SectionCode code, Period period, Day day,
                   CourseCode course, ClassroomRef classroom, ProfessorId professor) {
        this.code = code;
        this.period = period;
        this.day = day;
        this.course = course;
        this.classroom = classroom;
        this.professor = professor;
    }

    public SectionCode code() { return code; }
    public Period period() { return period; }
    public Day day() { return day; }
    public CourseCode course() { return course; }
    public ClassroomRef classroom() { return classroom; }
    public ProfessorId professor() { return professor; }

    /** Enrollments in enrollment order. */
    public List<Enrollment> enrollments() { return Collections.unmodifiableList(enrollments); }

    /** True when current enrollment count is less than capacity (R29). */
    public boolean hasRoom(int capacity) { return enrollments.size() < capacity; }

    /**
     * Add a new enrollment. Enforces capacity (I18).
     * The caller (EnrollmentService) has already checked I19 (one per course).
     */
    public void enroll(StudentNumber student, Grades grades, int capacity) {
        if (!hasRoom(capacity))
            throw new RuleViolation("section " + code + " has no room");
        enrollments.add(new Enrollment(student, grades));
    }

    /** Change the grades of an existing enrollment. */
    public void changeGrades(StudentNumber student, Grades grades) {
        findEnrollment(student).changeGrades(grades);
    }

    /** Remove an enrollment. */
    public void unenroll(StudentNumber student) {
        if (!enrollments.removeIf(e -> e.student().equals(student)))
            throw new RuleViolation("student " + student + " is not enrolled in section " + code);
    }

    /** Count of students currently enrolled. */
    public int enrollmentCount() { return enrollments.size(); }

    /** Find enrollment for a student, or throw. */
    private Enrollment findEnrollment(StudentNumber student) {
        return enrollments.stream()
                .filter(e -> e.student().equals(student))
                .findFirst()
                .orElseThrow(() -> new RuleViolation("student " + student + " is not enrolled in section " + code));
    }
}
