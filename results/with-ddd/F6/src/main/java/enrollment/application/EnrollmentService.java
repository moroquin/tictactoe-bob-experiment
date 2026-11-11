package enrollment.application;

import enrollment.domain.RuleViolation;
import enrollment.domain.access.Action;
import enrollment.domain.campus.ClassroomRef;
import enrollment.domain.catalog.CourseCode;
import enrollment.domain.catalog.CreditLimit;
import enrollment.domain.people.StudentNumber;
import enrollment.domain.sections.*;

import java.util.List;
import java.util.Optional;

/**
 * Application service for Enrollment use cases (I18, I19, I20, I31, I32, I33).
 */
public class EnrollmentService {

    private final AccessService access;
    private final CampusService campus;
    private final CatalogService catalog;
    private final PeopleService people;
    private final SectionRepository sections;
    private final SectionChoicePolicy choicePolicy;
    private final GradeScale gradeScale;

    public EnrollmentService(AccessService access, CampusService campus,
                             CatalogService catalog, PeopleService people,
                             SectionRepository sections,
                             SectionChoicePolicy choicePolicy, GradeScale gradeScale) {
        this.access = access;
        this.campus = campus;
        this.catalog = catalog;
        this.people = people;
        this.sections = sections;
        this.choicePolicy = choicePolicy;
        this.gradeScale = gradeScale;
    }

    /**
     * Enroll a student in a course (admin/staff). Returns the section the student was placed in.
     * R29, R30, I18, I19, I31, I32, I33.
     */
    public Section enroll(long studentNumber, long courseCode, int coursework, int finalExam) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_ENROLLMENTS);
        return doEnroll(studentNumber, courseCode, coursework, finalExam);
    }

    /**
     * A student enrolls themselves (R31, I20, I33).
     */
    public Section enrollMe(long courseCode) {
        access.requireSession();
        access.requirePermission(Action.ENROLL_SELF);
        // The logged-in user must be a student linked to a student record
        var user = access.session().user()
                .orElseThrow(() -> new RuleViolation("log in first"));
        StudentNumber sn = user.studentNumber()
                .orElseThrow(() -> new RuleViolation("user is not linked to a student"));
        return doEnroll(sn.value(), courseCode, 0, 0);
    }

    /** Change the grades of an enrolled student (R32). */
    public void changeGrades(long studentNumber, long courseCode, int coursework, int finalExam) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_ENROLLMENTS);
        StudentNumber sn = new StudentNumber(studentNumber);
        CourseCode cc = new CourseCode(courseCode);
        people.requireStudent(studentNumber);
        catalog.requireCourse(courseCode);
        Section section = sections.enrolledSection(sn, cc)
                .orElseThrow(() -> new RuleViolation("student " + studentNumber + " is not enrolled in course " + courseCode));
        Grades grades = gradeScale.grades(coursework, finalExam);
        section.changeGrades(sn, grades);
    }

    /** Unenroll a student from a course (R32). */
    public void unenroll(long studentNumber, long courseCode) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_ENROLLMENTS);
        StudentNumber sn = new StudentNumber(studentNumber);
        CourseCode cc = new CourseCode(courseCode);
        people.requireStudent(studentNumber);
        catalog.requireCourse(courseCode);
        Section section = sections.enrolledSection(sn, cc)
                .orElseThrow(() -> new RuleViolation("student " + studentNumber + " is not enrolled in course " + courseCode));
        section.unenroll(sn);
    }

    // ── Private ───────────────────────────────────────────────────────────────

    /**
     * Core enrollment logic. Order of checks follows I33:
     * 1. Student and course exist.
     * 2. Not already enrolled.
     * 3. Grades in range (only when coursework/finalExam are passed — Enroll; for EnrollMe
     *    they are 0/0 and will pass validation regardless).
     * 4. Prerequisites (I31).
     * 5. Credit limit (I32).
     * 6. Section with room (I18).
     */
    private Section doEnroll(long studentNumber, long courseCode, int coursework, int finalExam) {
        StudentNumber sn = new StudentNumber(studentNumber);
        CourseCode cc = new CourseCode(courseCode);
        // 1. Existence checks
        people.requireStudent(studentNumber);
        var course = catalog.requireCourse(courseCode);
        // 2. I19: one enrollment per course
        if (sections.enrolledSection(sn, cc).isPresent())
            throw new RuleViolation("student " + studentNumber + " is already enrolled in course " + courseCode);
        // 3. I17: validate grades
        Grades grades = gradeScale.grades(coursework, finalExam);
        // 4. Prerequisites (I31): student must have a passing enrollment in each prerequisite
        for (CourseCode prereq : course.prerequisites()) {
            Optional<Section> prereqSection = sections.enrolledSection(sn, prereq);
            boolean passed = prereqSection
                    .flatMap(s -> s.enrollments().stream()
                            .filter(e -> e.student().equals(sn))
                            .findFirst())
                    .map(e -> gradeScale.passed(e.grades()))
                    .orElse(false);
            if (!passed) {
                throw new RuleViolation("student " + studentNumber
                        + " has not passed prerequisite course " + prereq.value());
            }
        }
        // 5. Credit limit (I32): adding this course's credits must not exceed the limit
        int currentTotal = catalog.currentCredits(sn);
        if (currentTotal + course.credits() > CreditLimit.MAX) {
            throw new RuleViolation("enrolling would exceed the credit limit of " + CreditLimit.MAX);
        }
        // 6. R29: choose section with room
        List<Section> courseSections = sections.ofCourse(cc);
        if (courseSections.isEmpty())
            throw new RuleViolation("course " + courseCode + " has no sections");
        Optional<Section> chosen = choicePolicy.choose(courseSections,
                ref -> campus.capacityOf(ref));
        if (chosen.isEmpty())
            throw new RuleViolation("no section of course " + courseCode + " has room");
        Section section = chosen.get();
        int capacity = campus.capacityOf(section.classroom());
        section.enroll(sn, grades, capacity);
        return section;
    }
}
