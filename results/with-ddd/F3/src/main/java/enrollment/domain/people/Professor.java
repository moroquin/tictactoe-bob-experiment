package enrollment.domain.people;

/** Professor aggregate root. */
public class Professor {

    private final ProfessorId id;
    private String name;
    private String address;

    public Professor(ProfessorId id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public ProfessorId id() { return id; }
    public String name() { return name; }
    public String address() { return address; }

    public void modify(String name, String address) {
        this.name = name;
        this.address = address;
    }
}
