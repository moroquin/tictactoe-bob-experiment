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

    // ── Prerequisites (CR-5, I29, I30) ────────────────────────────────────────

    /**
     * Returns the prerequisite codes of the given course, in the order they were added.
     * Returns an empty list if the course has no prerequisites or does not exist.
     */
    List<CourseCode> prerequisitesOf(CourseCode course);

    /**
     * Records that {@code prerequisite} is a prerequisite of {@code course}.
     * The caller (CatalogService) has already validated all invariants (I29).
     */
    void addPrerequisite(CourseCode course, CourseCode prerequisite);

    /**
     * Removes all prerequisites of the given course (I30: used when deleting a course).
     */
    void removeAllPrerequisites(CourseCode course);

    /**
     * Returns {@code true} if the given course is a prerequisite of any other course (I30).
     */
    boolean isPrerequisiteOfAny(CourseCode code);
}
