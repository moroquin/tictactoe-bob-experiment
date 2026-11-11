package enrollment.model;

public class User {
    private final int id;
    private String name;
    private String password;
    private UserType type;
    private Long studentNumber; // only when type == STUDENT

    public User(int id, String name, String password, UserType type, Long studentNumber) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.type = type;
        this.studentNumber = studentNumber;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public UserType getType() { return type; }
    public Long getStudentNumber() { return studentNumber; }

    public void setName(String name) { this.name = name; }
    public void setPassword(String password) { this.password = password; }
    public void setType(UserType type) { this.type = type; }
    public void setStudentNumber(Long studentNumber) { this.studentNumber = studentNumber; }
}
