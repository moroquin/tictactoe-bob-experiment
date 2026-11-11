package enrollment.application;

import enrollment.domain.RuleViolation;
import enrollment.domain.access.Action;
import enrollment.domain.catalog.*;
import enrollment.domain.people.StudentNumber;
import enrollment.domain.sections.SectionRepository;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/** Application service for Course use cases. */
public class CatalogService {

    private final AccessService access;
    private final CourseRepository courses;
    private final SectionRepository sections;

    public CatalogService(AccessService access, CourseRepository courses,
                          SectionRepository sections) {
        this.access = access;
        this.courses = courses;
        this.sections = sections;
    }

    public Course createCourse(long code, String name, int semester, int credits) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        CourseCode cc = new CourseCode(code);
        if (courses.findByCode(cc).isPresent())
            throw new RuleViolation("course " + code + " already exists");
        Course c = new Course(cc, name, new Semester(semester), credits);
        courses.add(c);
        return c;
    }

    /**
     * Modify a course. Raises the credits only if no enrolled student would exceed the
     * credit limit (I32, CR-5).
     */
    public Course modifyCourse(long code, String name, int semester, int credits) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        Course c = requireCourse(code);
        int oldCredits = c.credits();
        // Validate the new Semester and credits by constructing the objects first
        Semester newSemester = new Semester(semester);
        if (credits <= 0) throw new RuleViolation("credits must be greater than 0");
        // If credits would increase, check that no enrolled student would exceed limit (I32)
        if (credits > oldCredits) {
            checkCreditLimitForModify(new CourseCode(code), oldCredits, credits);
        }
        c.modify(name, newSemester, credits);
        return c;
    }

    /**
     * Checks that raising this course's credits from oldCredits to newCredits would not
     * push any currently enrolled student above the credit limit (I32).
     */
    private void checkCreditLimitForModify(CourseCode cc, int oldCredits, int newCredits) {
        int delta = newCredits - oldCredits;
        // Find all students enrolled in any section of this course
        for (var section : sections.ofCourse(cc)) {
            for (var enrollment : section.enrollments()) {
                StudentNumber sn = enrollment.student();
                // Sum the student's current credits
                int currentTotal = currentCredits(sn);
                if (currentTotal + delta > CreditLimit.MAX) {
                    throw new RuleViolation("modifying credits would exceed the credit limit for student " + sn.value());
                }
            }
        }
    }

    /** Sum the credits of all courses a student is currently enrolled in. */
    public int currentCredits(StudentNumber sn) {
        int total = 0;
        for (CourseCode cc : sections.enrolledCourseCodes(sn)) {
            var course = courses.findByCode(cc);
            if (course.isPresent()) total += course.get().credits();
        }
        return total;
    }

    public void deleteCourse(long code) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        CourseCode cc = new CourseCode(code);
        requireCourse(code);
        if (!sections.ofCourse(cc).isEmpty())
            throw new RuleViolation("course " + code + " has sections");
        // I30: a course that is a prerequisite of another cannot be deleted
        if (courses.isPrerequisiteOfAny(cc))
            throw new RuleViolation("course " + code + " is a prerequisite of another course");
        // I30: if the course has prerequisites, remove them before deleting
        courses.removeAllPrerequisites(cc);
        courses.remove(cc);
    }

    public Course queryCourse(long code) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        return requireCourse(code);
    }

    public List<Course> listCourses() {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        return courses.listAll();
    }

    /**
     * Add a prerequisite to a course (CR-5, I29).
     * Both must exist and be different; the pair must not exist already; no cycles.
     */
    public void addPrerequisite(long courseCode, long prerequisiteCode) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        CourseCode cc = new CourseCode(courseCode);
        CourseCode prereqCc = new CourseCode(prerequisiteCode);
        requireCourse(courseCode);
        requireCourse(prerequisiteCode);
        if (cc.equals(prereqCc))
            throw new RuleViolation("a course cannot be its own prerequisite");
        // Check the pair does not already exist
        if (courses.prerequisitesOf(cc).contains(prereqCc))
            throw new RuleViolation("prerequisite already exists");
        // Cycle check: would adding prereqCc as prerequisite of cc create a cycle?
        // A cycle exists if cc is reachable from prereqCc following the prerequisite graph.
        if (wouldCreateCycle(cc, prereqCc))
            throw new RuleViolation("adding this prerequisite would create a cycle");
        courses.addPrerequisite(cc, prereqCc);
    }

    /**
     * Returns true if adding {@code prereqCc} as a prerequisite of {@code cc} would
     * create a cycle (i.e., if {@code cc} is reachable from {@code prereqCc} in the
     * prerequisite graph — I29).
     */
    private boolean wouldCreateCycle(CourseCode cc, CourseCode prereqCc) {
        // BFS from prereqCc: if we ever reach cc, it would create a cycle
        Set<CourseCode> visited = new HashSet<>();
        Queue<CourseCode> queue = new ArrayDeque<>();
        queue.add(prereqCc);
        while (!queue.isEmpty()) {
            CourseCode current = queue.poll();
            if (current.equals(cc)) return true;
            if (!visited.add(current)) continue;
            queue.addAll(courses.prerequisitesOf(current));
        }
        return false;
    }

    public Course requireCourse(long code) {
        CourseCode cc = new CourseCode(code);
        return courses.findByCode(cc)
                .orElseThrow(() -> new RuleViolation("course " + code + " does not exist"));
    }
}
