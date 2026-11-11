package enrollment.model;

public class Professor {
    private final long professorId;
    private String name;
    private String address;

    public Professor(long professorId, String name, String address) {
        this.professorId = professorId;
        this.name = name;
        this.address = address;
    }

    public long getProfessorId() { return professorId; }
    public String getName() { return name; }
    public String getAddress() { return address; }

    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
}
