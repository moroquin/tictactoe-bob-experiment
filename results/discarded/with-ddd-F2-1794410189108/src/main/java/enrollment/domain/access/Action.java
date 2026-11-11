package enrollment.domain.access;

/** An action that requires a specific Permission. */
public enum Action {
    MANAGE_USERS,
    MANAGE_SCHOOL_DATA,
    MANAGE_SECTIONS,
    MANAGE_ENROLLMENTS,
    ENROLL_SELF
}
