package enrollment.domain.sections;

import enrollment.domain.RuleViolation;

/**
 * Grade scale policy (I17, R28, policies.md).
 * Coursework: 0–70. Final exam: 0–30. Passing grade: 65.
 * This is the only class that knows these numbers. (CR-4 replaces old scale.)
 */
public class GradeScale {

    public static final int MAX_COURSEWORK = 70;
    public static final int MAX_FINAL_EXAM = 30;
    public static final int PASSING_GRADE  = 65;

    public int passingGrade() { return PASSING_GRADE; }

    /** Validate and build a Grades value object. */
    public Grades grades(int coursework, int finalExam) {
        if (coursework < 0 || coursework > MAX_COURSEWORK)
            throw new RuleViolation("coursework must be from 0 to " + MAX_COURSEWORK);
        if (finalExam < 0 || finalExam > MAX_FINAL_EXAM)
            throw new RuleViolation("final exam must be from 0 to " + MAX_FINAL_EXAM);
        return new Grades(coursework, finalExam);
    }

    public boolean passed(Grades grades) {
        return grades.grade() >= PASSING_GRADE;
    }
}
