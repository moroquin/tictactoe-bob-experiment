package enrollment.console;

import enrollment.domain.access.User;
import enrollment.domain.campus.Building;
import enrollment.domain.campus.Classroom;
import enrollment.domain.catalog.Course;
import enrollment.domain.people.Professor;
import enrollment.domain.people.Student;
import enrollment.domain.sections.Section;
import enrollment.application.ReportService;
import enrollment.application.SectionService;

import java.util.List;

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

    // ── Reports (CR-2) ────────────────────────────────────────────────────────

    /**
     * Format StudentCourses rows or the no-results message (I25).
     * One line per row: courseCode | course name | section <code> | coursework <c> | final <f> | passed/failed
     */
    public String formatStudentCourses(List<ReportService.StudentCourseRow> rows) {
        if (rows.isEmpty()) return "No courses";
        var sb = new StringBuilder();
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) sb.append('\n');
            var r = rows.get(i);
            sb.append(r.courseCode()).append(" | ").append(r.courseName())
              .append(" | section ").append(r.sectionCode())
              .append(" | coursework ").append(r.coursework())
              .append(" | final ").append(r.finalExam())
              .append(" | ").append(r.passed() ? "passed" : "failed");
        }
        return sb.toString();
    }

    /**
     * Format CourseStudents rows or the no-results message (I26).
     * One line per row: studentNumber | name | section <code> | coursework <c> | final <f> | passed/failed
     */
    public String formatCourseStudents(List<ReportService.CourseStudentRow> rows) {
        if (rows.isEmpty()) return "No students";
        var sb = new StringBuilder();
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) sb.append('\n');
            var r = rows.get(i);
            sb.append(r.studentNumber()).append(" | ").append(r.studentName())
              .append(" | section ").append(r.sectionCode())
              .append(" | coursework ").append(r.coursework())
              .append(" | final ").append(r.finalExam())
              .append(" | ").append(r.passed() ? "passed" : "failed");
        }
        return sb.toString();
    }

    /**
     * Format ClassroomSections rows or the no-results message (I27).
     * One line per row: section <code> | day | period | courseCode | course name
     */
    public String formatClassroomSections(List<ReportService.ClassroomSectionRow> rows) {
        if (rows.isEmpty()) return "No sections";
        var sb = new StringBuilder();
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) sb.append('\n');
            var r = rows.get(i);
            sb.append("section ").append(r.sectionCode())
              .append(" | ").append(r.day())
              .append(" | ").append(r.period())
              .append(" | ").append(r.courseCode())
              .append(" | ").append(r.courseName());
        }
        return sb.toString();
    }

    /**
     * Format PassedBySemester rows and totals or the no-results message (I28).
     * One line per course: courseCode | course name | passed <p> | failed <f>
     * Last line: Total | passed <P> | failed <F>
     */
    public String formatPassedBySemester(List<ReportService.SemesterCourseRow> rows) {
        if (rows.isEmpty()) return "No courses in the semester";
        var sb = new StringBuilder();
        int totalPassed = 0, totalFailed = 0;
        for (var r : rows) {
            sb.append(r.courseCode()).append(" | ").append(r.courseName())
              .append(" | passed ").append(r.passed())
              .append(" | failed ").append(r.failed())
              .append('\n');
            totalPassed += r.passed();
            totalFailed += r.failed();
        }
        sb.append("Total | passed ").append(totalPassed)
          .append(" | failed ").append(totalFailed);
        return sb.toString();
    }
}
