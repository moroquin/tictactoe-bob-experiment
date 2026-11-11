package enrollment.model;

import java.util.ArrayList;
import java.util.List;

public class Section {
    private final long code;
    private final String period;
    private final String day;
    private final Course course;
    private final Classroom classroom;
    private final Professor professor;
    private final List<Enrollment> enrollments = new ArrayList<>();

    public Section(long code, String period, String day, Course course,
                   Classroom classroom, Professor professor) {
        this.code = code;
        this.period = period;
        this.day = day;
        this.course = course;
        this.classroom = classroom;
        this.professor = professor;
    }

    public long getCode() { return code; }
    public String getPeriod() { return period; }
    public String getDay() { return day; }
    public Course getCourse() { return course; }
    public Classroom getClassroom() { return classroom; }
    public Professor getProfessor() { return professor; }
    public List<Enrollment> getEnrollments() { return enrollments; }

    public int enrolledCount() { return enrollments.size(); }

    public boolean hasRoom() {
        return enrollments.size() < classroom.getCapacity();
    }
}
