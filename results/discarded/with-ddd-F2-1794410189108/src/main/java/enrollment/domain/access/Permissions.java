package enrollment.domain.access;

/**
 * Permission policy: what each UserType may do (I5, R7, policies.md).
 */
public class Permissions {

    public boolean allows(UserType type, Action action) {
        return switch (action) {
            case MANAGE_USERS        -> type == UserType.ADMIN;
            case MANAGE_SCHOOL_DATA  -> type == UserType.ADMIN || type == UserType.STAFF;
            case MANAGE_SECTIONS     -> type == UserType.ADMIN || type == UserType.STAFF;
            case MANAGE_ENROLLMENTS  -> type == UserType.ADMIN || type == UserType.STAFF;
            case ENROLL_SELF         -> type == UserType.STUDENT;
        };
    }
}
