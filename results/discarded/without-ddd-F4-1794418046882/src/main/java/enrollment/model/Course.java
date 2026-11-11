package enrollment.model;

public class Course {
    private final long code;
    private String name;
    private int semester;
    private int credits;

    public Course(long code, String name, int semester, int credits) {
        this.code = code;
        this.name = name;
        this.semester = semester;
        this.credits = credits;
    }

    public long getCode() { return code; }
    public String getName() { return name; }
    public int getSemester() { return semester; }
    public int getCredits() { return credits; }

    public void setName(String name) { this.name = name; }
    public void setSemester(int semester) { this.semester = semester; }
    public void setCredits(int credits) { this.credits = credits; }
}
