package enrollment.domain.sections;

import enrollment.domain.RuleViolation;

/**
 * Grade scale policy (I17, R28, policies.md).
 * Coursework: 0–75. Final exam: 0–25. Passing grade: 61.
 * This is the only class that knows these numbers.
 */
public class GradeScale {

    public static final int MAX_COURSEWORK = 75;
    public static final int MAX_FINAL_EXAM = 25;
    public static final int PASSING_GRADE  = 61;

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
