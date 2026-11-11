package enrollment.domain.access;

/** An action that requires a specific Permission. */
public enum Action {
    MANAGE_USERS,
    MANAGE_SCHOOL_DATA,
    MANAGE_SECTIONS,
    MANAGE_ENROLLMENTS,
    ENROLL_SELF,
    /** View enrollment reports. Students may only query their own student number (I25). */
    QUERY_ENROLLMENTS,
    /** Load instructions from a file (admin only, I24). */
    LOAD_FILE
}
