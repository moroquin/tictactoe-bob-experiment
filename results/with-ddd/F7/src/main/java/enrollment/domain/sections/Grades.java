package enrollment.domain.sections;

/** Value object: the grades of an enrollment (I17, R28). */
public record Grades(int coursework, int finalExam) {

    /** Sum of coursework and final exam. */
    public int grade() { return coursework + finalExam; }

    public boolean passed() { return grade() >= GradeScale.PASSING_GRADE; }
}
