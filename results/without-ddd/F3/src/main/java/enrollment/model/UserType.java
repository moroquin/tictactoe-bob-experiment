package enrollment.model;

public enum UserType {
    ADMIN, STAFF, STUDENT;

    public static UserType parse(String s) {
        return switch (s.toLowerCase()) {
            case "admin"   -> ADMIN;
            case "staff"   -> STAFF;
            case "student" -> STUDENT;
            default        -> null;
        };
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
