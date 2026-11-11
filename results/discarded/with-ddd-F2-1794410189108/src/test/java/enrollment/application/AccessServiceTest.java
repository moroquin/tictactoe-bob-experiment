package enrollment.application;

import enrollment.domain.RuleViolation;
import enrollment.domain.access.*;
import enrollment.domain.people.StudentNumber;
import enrollment.infrastructure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for AccessService use cases (I3, I4, I5, I7, I8, I9, I10). */
class AccessServiceTest {

    private AccessService service;
    private InMemoryUserRepository users;

    @BeforeEach
    void setup() {
        users = new InMemoryUserRepository();
        var studentRepo = new InMemoryStudentRepository();
        var session = new Session();
        var permissions = new Permissions();
        service = new AccessService(session, users, studentRepo, permissions);
        // Seed initial admin (I3)
        users.add(new User(1L, "admin", "admin", UserType.ADMIN, null));
    }

    // ── I3: initial admin ─────────────────────────────────────────────────────

    @Test
    void I3_initial_admin_exists_and_has_id_1() {
        service.login("admin", "admin");
        User u = service.queryUser(1L);
        assertEquals("admin", u.name());
        assertEquals(UserType.ADMIN, u.type());
    }

    @Test
    void I3_initial_admin_cannot_be_deleted() {
        service.login("admin", "admin");
        assertThrows(RuleViolation.class, () -> service.deleteUser(1L));
    }

    // ── I4: session ───────────────────────────────────────────────────────────

    @Test
    void I4_cannot_login_twice() {
        service.login("admin", "admin");
        assertThrows(RuleViolation.class, () -> service.login("admin", "admin"));
    }

    @Test
    void I4_no_session_gives_log_in_first_error() {
        RuleViolation ex = assertThrows(RuleViolation.class, () -> service.queryUser(1L));
        assertEquals("log in first", ex.reason());
    }

    @Test
    void I4_wrong_password_fails() {
        assertThrows(RuleViolation.class, () -> service.login("admin", "wrong"));
    }

    // ── I5: permissions ───────────────────────────────────────────────────────

    @Test
    void I5_staff_cannot_manage_users() {
        users.add(new User(2L, "staff1", "pass", UserType.STAFF, null));
        service.login("admin", "admin");
        service.logout();
        service.login("staff1", "pass");
        RuleViolation ex = assertThrows(RuleViolation.class,
                () -> service.queryUser(1L));
        assertEquals("permission denied", ex.reason());
    }

    // ── I7: IDs never reused ──────────────────────────────────────────────────

    @Test
    void I7_user_ids_assigned_ascending_and_not_reused() {
        service.login("admin", "admin");
        User u2 = service.createUser("u2", "p", UserType.STAFF, Optional.empty());
        assertEquals(2L, u2.id());
        User u3 = service.createUser("u3", "p", UserType.STAFF, Optional.empty());
        assertEquals(3L, u3.id());
        service.deleteUser(2L);
        User u4 = service.createUser("u4", "p", UserType.STAFF, Optional.empty());
        // Must be 4, not 2
        assertEquals(4L, u4.id());
    }

    // ── I8: unique user names ─────────────────────────────────────────────────

    @Test
    void I8_duplicate_user_name_rejected() {
        service.login("admin", "admin");
        service.createUser("alice", "p", UserType.STAFF, Optional.empty());
        assertThrows(RuleViolation.class,
                () -> service.createUser("alice", "p2", UserType.STAFF, Optional.empty()));
    }

    // ── I9: student link ──────────────────────────────────────────────────────

    @Test
    void I9_student_type_requires_student_number() {
        service.login("admin", "admin");
        assertThrows(RuleViolation.class,
                () -> service.createUser("bob", "p", UserType.STUDENT, Optional.empty()));
    }

    @Test
    void I9_non_student_type_rejects_student_number() {
        service.login("admin", "admin");
        assertThrows(RuleViolation.class,
                () -> service.createUser("bob", "p", UserType.STAFF,
                        Optional.of(new StudentNumber(100L))));
    }

    // ── I10: cannot delete session user ───────────────────────────────────────

    @Test
    void I10_cannot_delete_current_session_user() {
        users.add(new User(2L, "admin2", "p", UserType.ADMIN, null));
        service.login("admin2", "p");
        assertThrows(RuleViolation.class, () -> service.deleteUser(2L));
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Test
    void I4_logout_closes_session() {
        service.login("admin", "admin");
        service.logout();
        assertThrows(RuleViolation.class, () -> service.queryUser(1L));
    }
}
