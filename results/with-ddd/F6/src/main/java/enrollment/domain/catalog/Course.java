package enrollment.domain.catalog;

import enrollment.domain.RuleViolation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Course aggregate root. */
public class Course {

    private final CourseCode code;
    private String name;
    private Semester semester;
    private int credits;
    /** Prerequisites in the order they were added (I29). */
    private final List<CourseCode> prerequisites = new ArrayList<>();

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

    /** The prerequisites of this course, in the order they were added. */
    public List<CourseCode> prerequisites() {
        return Collections.unmodifiableList(prerequisites);
    }

    /** Add a prerequisite code. The caller must have checked all invariants (I29). */
    public void addPrerequisite(CourseCode prerequisite) {
        prerequisites.add(prerequisite);
    }

    /** Remove all prerequisites (used when deleting a course — I30). */
    public void removeAllPrerequisites() {
        prerequisites.clear();
    }

    public void modify(String name, Semester semester, int credits) {
        if (credits <= 0) throw new RuleViolation("credits must be greater than 0");
        this.name = name;
        this.semester = semester;
        this.credits = credits;
    }
}
