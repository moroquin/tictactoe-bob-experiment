package enrollment.model;

public class Enrollment {
    private final Student student;
    private final Section section;
    private int coursework;
    private int finalExam;

    public Enrollment(Student student, Section section, int coursework, int finalExam) {
        this.student = student;
        this.section = section;
        this.coursework = coursework;
        this.finalExam = finalExam;
    }

    public Student getStudent() { return student; }
    public Section getSection() { return section; }
    public int getCoursework() { return coursework; }
    public int getFinalExam() { return finalExam; }

    public void setCoursework(int coursework) { this.coursework = coursework; }
    public void setFinalExam(int finalExam) { this.finalExam = finalExam; }

    public int getGrade() { return coursework + finalExam; }
    public boolean passes() { return getGrade() >= 65; }
}
