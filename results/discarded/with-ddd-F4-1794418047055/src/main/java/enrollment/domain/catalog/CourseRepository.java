package enrollment.domain.catalog;

import java.util.List;
import java.util.Optional;

/** Repository for Courses. */
public interface CourseRepository {

    void add(Course course);

    Optional<Course> findByCode(CourseCode code);

    /** All courses in creation order. */
    List<Course> listAll();

    void remove(CourseCode code);
}
