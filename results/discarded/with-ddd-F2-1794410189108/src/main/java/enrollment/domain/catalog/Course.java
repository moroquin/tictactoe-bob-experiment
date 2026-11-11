package enrollment.domain.catalog;

import enrollment.domain.RuleViolation;

/** Course aggregate root. */
public class Course {

    private final CourseCode code;
    private String name;
    private Semester semester;
    private int credits;

    public Course(CourseCode code, String name, Semester semester, int credits) {
        if (credits <= 0) throw new RuleViolation("credits must be greater than 0");
        this.code = code;
        this.name = name;
        this.semester = semester;
        this.credits = credits;
    }

    public CourseCode code() { return code; }
    public String name() { return name; }
    public Semester semester() { return semester; }
    public int credits() { return credits; }

    public void modify(String name, Semester semester, int credits) {
        if (credits <= 0) throw new RuleViolation("credits must be greater than 0");
        this.name = name;
        this.semester = semester;
        this.credits = credits;
    }
}
