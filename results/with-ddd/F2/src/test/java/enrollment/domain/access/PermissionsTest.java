package enrollment.domain.access;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for Permissions policy (I5, R7). */
class PermissionsTest {

    private final Permissions permissions = new Permissions();

    @Test
    void I5_admin_can_manage_users() {
        assertTrue(permissions.allows(UserType.ADMIN, Action.MANAGE_USERS));
    }

    @Test
    void I5_staff_cannot_manage_users() {
        assertFalse(permissions.allows(UserType.STAFF, Action.MANAGE_USERS));
    }

    @Test
    void I5_student_cannot_manage_users() {
        assertFalse(permissions.allows(UserType.STUDENT, Action.MANAGE_USERS));
    }

    @Test
    void I5_admin_can_manage_school_data() {
        assertTrue(permissions.allows(UserType.ADMIN, Action.MANAGE_SCHOOL_DATA));
    }

    @Test
    void I5_staff_can_manage_school_data() {
        assertTrue(permissions.allows(UserType.STAFF, Action.MANAGE_SCHOOL_DATA));
    }

    @Test
    void I5_student_cannot_manage_school_data() {
        assertFalse(permissions.allows(UserType.STUDENT, Action.MANAGE_SCHOOL_DATA));
    }

    @Test
    void I5_admin_can_manage_sections() {
        assertTrue(permissions.allows(UserType.ADMIN, Action.MANAGE_SECTIONS));
    }

    @Test
    void I5_staff_can_manage_sections() {
        assertTrue(permissions.allows(UserType.STAFF, Action.MANAGE_SECTIONS));
    }

    @Test
    void I5_student_cannot_manage_sections() {
        assertFalse(permissions.allows(UserType.STUDENT, Action.MANAGE_SECTIONS));
    }

    @Test
    void I5_admin_can_manage_enrollments() {
        assertTrue(permissions.allows(UserType.ADMIN, Action.MANAGE_ENROLLMENTS));
    }

    @Test
    void I5_staff_can_manage_enrollments() {
        assertTrue(permissions.allows(UserType.STAFF, Action.MANAGE_ENROLLMENTS));
    }

    @Test
    void I5_student_cannot_manage_enrollments() {
        assertFalse(permissions.allows(UserType.STUDENT, Action.MANAGE_ENROLLMENTS));
    }

    @Test
    void I5_only_student_can_enroll_self() {
        assertFalse(permissions.allows(UserType.ADMIN, Action.ENROLL_SELF));
        assertFalse(permissions.allows(UserType.STAFF, Action.ENROLL_SELF));
        assertTrue(permissions.allows(UserType.STUDENT, Action.ENROLL_SELF));
    }
}
