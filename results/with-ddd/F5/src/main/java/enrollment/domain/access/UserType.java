package enrollment.domain.access;

/** The three types of user in the system. */
public enum UserType {
    ADMIN, STAFF, STUDENT;

    /** Parse a user type from a string (case-insensitive). */
    public static UserType parse(String value) {
        return switch (value.toLowerCase()) {
            case "admin"   -> ADMIN;
            case "staff"   -> STAFF;
            case "student" -> STUDENT;
            default -> throw new enrollment.domain.RuleViolation("unknown user type: " + value);
        };
    }

    /** The lower-case name used in output. */
    public String display() {
        return name().toLowerCase();
    }
}
