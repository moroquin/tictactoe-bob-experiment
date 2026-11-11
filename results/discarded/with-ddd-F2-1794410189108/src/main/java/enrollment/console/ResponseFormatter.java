package enrollment.console;

import enrollment.domain.access.User;
import enrollment.domain.campus.Building;
import enrollment.domain.campus.Classroom;
import enrollment.domain.catalog.Course;
import enrollment.domain.people.Professor;
import enrollment.domain.people.Student;
import enrollment.domain.sections.Section;
import enrollment.application.SectionService;

/**
 * Formats query responses as described in REQUIREMENTS.md section 4.3.
 * Data is separated by " | ".
 */
public class ResponseFormatter {

    private final SectionService sectionService;

    public ResponseFormatter(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    /** Format a User query. Password is never shown (R, section 4.3). */
    public String formatUser(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append(user.id()).append(" | ").append(user.name()).append(" | ").append(user.type().display());
        user.studentNumber().ifPresent(sn -> sb.append(" | ").append(sn.value()));
        return sb.toString();
    }

    /** Format a Building query: name and classrooms. */
    public String formatBuilding(Building building) {
        StringBuilder sb = new StringBuilder();
        sb.append(building.name()).append(" | ");
        var classrooms = building.classrooms(); // ascending number order
        if (classrooms.isEmpty()) {
            sb.append("no classrooms");
        } else {
            for (int i = 0; i < classrooms.size(); i++) {
                Classroom c = classrooms.get(i);
                if (i > 0) sb.append(", ");
                sb.append(c.number()).append(" (").append(c.capacity()).append(")");
            }
        }
        return sb.toString();
    }

    /** Format a Course query. */
    public String formatCourse(Course course) {
        return course.code() + " | " + course.name() + " | "
                + course.semester().value() + " | " + course.credits();
    }

    /** Format a Student query. */
    public String formatStudent(Student student) {
        return student.number().value() + " | " + student.name() + " | " + student.address();
    }

    /** Format a Professor query. */
    public String formatProfessor(Professor professor) {
        return professor.id().value() + " | " + professor.name() + " | " + professor.address();
    }

    /**
     * Format a Section query:
     * code | period | day | courseCode | classroomNumber | buildingName | professorId | enrolled/capacity
     */
    public String formatSection(Section section) {
        String buildingName = sectionService.buildingNameOf(section);
        Classroom classroom = sectionService.classroomOf(section);
        int capacity = classroom != null ? classroom.capacity() : 0;
        int enrolled = section.enrollmentCount();
        return section.code() + " | " + section.period() + " | " + section.day().display()
                + " | " + section.course() + " | " + section.classroom().number()
                + " | " + buildingName
                + " | " + section.professor()
                + " | " + enrolled + "/" + capacity;
    }
}
