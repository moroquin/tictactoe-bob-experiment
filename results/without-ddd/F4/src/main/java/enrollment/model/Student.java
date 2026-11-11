package enrollment.model;

public class Student {
    private final long studentNumber;
    private String name;
    private String address;

    public Student(long studentNumber, String name, String address) {
        this.studentNumber = studentNumber;
        this.name = name;
        this.address = address;
    }

    public long getStudentNumber() { return studentNumber; }
    public String getName() { return name; }
    public String getAddress() { return address; }

    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
}
