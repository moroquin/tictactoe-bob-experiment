package enrollment.domain.sections;

import enrollment.domain.RuleViolation;

/** Day of the week (I15, R25). Stored in lower case. */
public enum Day {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

    public static Day parse(String text) {
        return switch (text.strip().toLowerCase()) {
            case "monday"    -> MONDAY;
            case "tuesday"   -> TUESDAY;
            case "wednesday" -> WEDNESDAY;
            case "thursday"  -> THURSDAY;
            case "friday"    -> FRIDAY;
            case "saturday"  -> SATURDAY;
            case "sunday"    -> SUNDAY;
            default -> throw new RuleViolation("invalid day: " + text);
        };
    }

    public String display() { return name().toLowerCase(); }
}
