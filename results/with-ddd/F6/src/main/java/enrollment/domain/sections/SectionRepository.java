package enrollment.domain.sections;

import enrollment.domain.campus.ClassroomRef;
import enrollment.domain.catalog.CourseCode;
import enrollment.domain.people.ProfessorId;
import enrollment.domain.people.StudentNumber;

import java.util.List;
import java.util.Optional;

/** Repository for Sections. */
public interface SectionRepository {

    void add(Section section);

    void remove(SectionCode code);

    Optional<Section> findByCode(SectionCode code);

    /** All sections in ascending code order. */
    List<Section> listAll();

    /** Sections of a course, in ascending code order. */
    List<Section> ofCourse(CourseCode courseCode);

    /** Sections that use a classroom. */
    List<Section> usingClassroom(ClassroomRef ref);

    /** Sections that use a professor. */
    List<Section> usingProfessor(ProfessorId id);

    /**
     * All enrollments for a student, across all sections.
     * Returns one Enrollment per course the student is enrolled in.
     */
    List<Enrollment> enrollmentsOf(StudentNumber student);

    /**
     * Find the section in which a student is enrolled for a given course, or empty.
     */
    Optional<Section> enrolledSection(StudentNumber student, CourseCode courseCode);

    /**
     * Returns the course codes of all courses the student is currently enrolled in (CR-5, I32).
     */
    List<CourseCode> enrolledCourseCodes(StudentNumber student);
}
