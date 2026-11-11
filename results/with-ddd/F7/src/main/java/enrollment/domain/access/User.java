package enrollment.domain.access;

import enrollment.domain.people.StudentNumber;
import java.util.Optional;

/** A person who can log in. Aggregate root of the Access context. */
public class User {

    private final long id;
    private String name;
    private String password;
    private UserType type;
    private StudentNumber studentNumber; // null when type != STUDENT

    public User(long id, String name, String password, UserType type, StudentNumber studentNumber) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.type = type;
        this.studentNumber = studentNumber;
    }

    public long id() { return id; }
    public String name() { return name; }
    public UserType type() { return type; }
    public Optional<StudentNumber> studentNumber() { return Optional.ofNullable(studentNumber); }

    public boolean checkPassword(String candidate) {
        return password.equals(candidate);
    }

    /** Mutate: change name, password, type and studentNumber. */
    public void modify(String name, String password, UserType type, StudentNumber studentNumber) {
        this.name = name;
        this.password = password;
        this.type = type;
        this.studentNumber = studentNumber;
    }
}
