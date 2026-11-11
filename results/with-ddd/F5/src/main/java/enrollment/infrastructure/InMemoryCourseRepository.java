package enrollment.infrastructure;

import enrollment.domain.catalog.Course;
import enrollment.domain.catalog.CourseCode;
import enrollment.domain.catalog.CourseRepository;

import java.util.*;

/** In-memory CourseRepository. Courses are listed in creation order. */
public class InMemoryCourseRepository implements CourseRepository {

    private final List<Course> courses = new ArrayList<>();

    @Override
    public void add(Course course) {
        courses.add(course);
    }

    @Override
    public Optional<Course> findByCode(CourseCode code) {
        return courses.stream().filter(c -> c.code().equals(code)).findFirst();
    }

    @Override
    public List<Course> listAll() {
        return Collections.unmodifiableList(courses);
    }

    @Override
    public void remove(CourseCode code) {
        courses.removeIf(c -> c.code().equals(code));
    }
}
