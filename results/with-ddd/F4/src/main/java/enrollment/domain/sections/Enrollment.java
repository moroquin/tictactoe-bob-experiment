package enrollment.domain.sections;

import enrollment.domain.people.StudentNumber;

/** Entity: a student in a section, with their grades. */
public class Enrollment {

    private final StudentNumber student;
    private Grades grades;

    public Enrollment(StudentNumber student, Grades grades) {
        this.student = student;
        this.grades = grades;
    }

    public StudentNumber student() { return student; }
    public Grades grades() { return grades; }

    public void changeGrades(Grades grades) {
        this.grades = grades;
    }
}
