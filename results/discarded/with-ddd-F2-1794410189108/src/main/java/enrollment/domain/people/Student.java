package enrollment.domain.people;

/** Student aggregate root. */
public class Student {

    private final StudentNumber number;
    private String name;
    private String address;

    public Student(StudentNumber number, String name, String address) {
        this.number = number;
        this.name = name;
        this.address = address;
    }

    public StudentNumber number() { return number; }
    public String name() { return name; }
    public String address() { return address; }

    public void modify(String name, String address) {
        this.name = name;
        this.address = address;
    }
}
