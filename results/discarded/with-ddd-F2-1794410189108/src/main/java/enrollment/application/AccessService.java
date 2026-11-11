package enrollment.application;

import enrollment.domain.RuleViolation;
import enrollment.domain.access.*;
import enrollment.domain.people.StudentNumber;
import enrollment.domain.people.StudentRepository;

import java.util.List;
import java.util.Optional;

/**
 * Application service for User/Session/Access use cases.
 * Checks: Session → Permission → rules (I6, D6).
 */
public class AccessService {

    private final Session session;
    private final UserRepository users;
    private final StudentRepository students;
    private final Permissions permissions;

    public AccessService(Session session, UserRepository users,
                         StudentRepository students, Permissions permissions) {
        this.session = session;
        this.users = users;
        this.students = students;
        this.permissions = permissions;
    }

    // ── Session ──────────────────────────────────────────────────────────────

    /** Login (R6, I4). */
    public void login(String name, String password) {
        if (session.isOpen())
            throw new RuleViolation("a session is already open");
        User user = users.findByName(name)
                .orElseThrow(() -> new RuleViolation("invalid credentials"));
        if (!user.checkPassword(password))
            throw new RuleViolation("invalid credentials");
        session.open(user);
    }

    /** Logout. */
    public void logout() {
        requireSession();
        session.close();
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    public User createUser(String name, String password, UserType type,
                           Optional<StudentNumber> studentNumber) {
        requireSession();
        requirePermission(Action.MANAGE_USERS);
        validateUserStudentNumber(type, studentNumber);
        if (users.findByName(name).isPresent())
            throw new RuleViolation("user name " + name + " already exists");
        if (type == UserType.STUDENT) {
            StudentNumber sn = studentNumber.get();
            requireStudentExists(sn);
            requireNoLinkedUser(sn, -1);
        }
        long id = users.nextId();
        User user = new User(id, name, password, type, studentNumber.orElse(null));
        users.add(user);
        return user;
    }

    public User modifyUser(long id, String name, String password, UserType type,
                           Optional<StudentNumber> studentNumber) {
        requireSession();
        requirePermission(Action.MANAGE_USERS);
        User user = requireUserExists(id);
        // Cannot change type of initial admin (I3)
        if (id == 1 && type != UserType.ADMIN)
            throw new RuleViolation("cannot change the type of the initial admin");
        validateUserStudentNumber(type, studentNumber);
        if (users.findByName(name).filter(u -> u.id() != id).isPresent())
            throw new RuleViolation("user name " + name + " already exists");
        if (type == UserType.STUDENT) {
            StudentNumber sn = studentNumber.get();
            requireStudentExists(sn);
            requireNoLinkedUser(sn, id);
        }
        user.modify(name, password, type, studentNumber.orElse(null));
        return user;
    }

    public void deleteUser(long id) {
        requireSession();
        requirePermission(Action.MANAGE_USERS);
        User user = requireUserExists(id);
        if (id == 1)
            throw new RuleViolation("cannot delete the initial admin");
        if (session.user().isPresent() && session.user().get().id() == id)
            throw new RuleViolation("cannot delete the user of the open session");
        users.remove(id);
    }

    public User queryUser(long id) {
        requireSession();
        requirePermission(Action.MANAGE_USERS);
        return requireUserExists(id);
    }

    public List<User> listUsers() {
        requireSession();
        requirePermission(Action.MANAGE_USERS);
        return users.listAll();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public User requireSession() {
        return session.user()
                .orElseThrow(() -> new RuleViolation("log in first"));
    }

    public void requirePermission(Action action) {
        User u = session.user()
                .orElseThrow(() -> new RuleViolation("log in first"));
        if (!permissions.allows(u.type(), action))
            throw new RuleViolation("permission denied");
    }

    private User requireUserExists(long id) {
        return users.findById(id)
                .orElseThrow(() -> new RuleViolation("user " + id + " does not exist"));
    }

    private void validateUserStudentNumber(UserType type, Optional<StudentNumber> studentNumber) {
        if (type == UserType.STUDENT && studentNumber.isEmpty())
            throw new RuleViolation("student number is required for user type student");
        if (type != UserType.STUDENT && studentNumber.isPresent())
            throw new RuleViolation("student number is not allowed for user type " + type.display());
    }

    private void requireStudentExists(StudentNumber sn) {
        students.findByNumber(sn)
                .orElseThrow(() -> new RuleViolation("student " + sn + " does not exist"));
    }

    private void requireNoLinkedUser(StudentNumber sn, long excludeUserId) {
        users.listAll().stream()
                .filter(u -> u.id() != excludeUserId)
                .filter(u -> u.studentNumber().isPresent() && u.studentNumber().get().equals(sn))
                .findFirst()
                .ifPresent(u -> { throw new RuleViolation("student " + sn + " already has a user"); });
    }

    /** Expose session for use by other services. */
    public Session session() { return session; }
    /** Expose permissions for use by other services. */
    public Permissions permissions() { return permissions; }
}
