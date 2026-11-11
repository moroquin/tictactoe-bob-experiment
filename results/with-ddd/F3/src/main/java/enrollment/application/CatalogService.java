package enrollment.application;

import enrollment.domain.RuleViolation;
import enrollment.domain.access.Action;
import enrollment.domain.catalog.*;
import enrollment.domain.sections.SectionRepository;

import java.util.List;

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

    public Course modifyCourse(long code, String name, int semester, int credits) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        Course c = requireCourse(code);
        c.modify(name, new Semester(semester), credits);
        return c;
    }

    public void deleteCourse(long code) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        CourseCode cc = new CourseCode(code);
        requireCourse(code);
        if (!sections.ofCourse(cc).isEmpty())
            throw new RuleViolation("course " + code + " has sections");
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

    public Course requireCourse(long code) {
        CourseCode cc = new CourseCode(code);
        return courses.findByCode(cc)
                .orElseThrow(() -> new RuleViolation("course " + code + " does not exist"));
    }
}
