package enrollment.domain.sections;

import enrollment.domain.RuleViolation;

/**
 * Value object: one of the two ends of a Period, h:mm am/pm.
 * Range: 12:00am (midnight) to 11:59pm (I14).
 * Stored as total minutes from midnight (0–719 for am, 720–1439 for pm).
 */
public record TimeOfDay(int minutesFromMidnight) implements Comparable<TimeOfDay> {

    public TimeOfDay {
        if (minutesFromMidnight < 0 || minutesFromMidnight > 23 * 60 + 59)
            throw new RuleViolation("invalid time of day");
    }

    /**
     * Parse a time such as "7:10am" or "12:00pm".
     * Hours 1–12, minutes 00–59, followed by am or pm (case-insensitive).
     */
    public static TimeOfDay parse(String text) {
        String t = text.strip().toLowerCase();
        boolean pm;
        if (t.endsWith("am")) pm = false;
        else if (t.endsWith("pm")) pm = true;
        else throw new RuleViolation("invalid time: " + text);
        String timePart = t.substring(0, t.length() - 2);
        int colonIdx = timePart.indexOf(':');
        if (colonIdx < 0) throw new RuleViolation("invalid time: " + text);
        int hours, minutes;
        try {
            hours = Integer.parseInt(timePart.substring(0, colonIdx));
            minutes = Integer.parseInt(timePart.substring(colonIdx + 1));
        } catch (NumberFormatException e) {
            throw new RuleViolation("invalid time: " + text);
        }
        if (hours < 1 || hours > 12) throw new RuleViolation("invalid time: " + text);
        if (minutes < 0 || minutes > 59) throw new RuleViolation("invalid time: " + text);
        // 12am = midnight = 0, 12pm = noon = 720
        int h24 = pm ? (hours == 12 ? 12 : hours + 12) : (hours == 12 ? 0 : hours);
        return new TimeOfDay(h24 * 60 + minutes);
    }

    /** Format as h:mm am/pm (lower case). */
    @Override
    public String toString() {
        int h24 = minutesFromMidnight / 60;
        int m = minutesFromMidnight % 60;
        boolean pm = h24 >= 12;
        int h12 = h24 % 12;
        if (h12 == 0) h12 = 12;
        return h12 + ":" + String.format("%02d", m) + (pm ? "pm" : "am");
    }

    public boolean isBefore(TimeOfDay other) {
        return this.minutesFromMidnight < other.minutesFromMidnight;
    }

    @Override
    public int compareTo(TimeOfDay other) {
        return Integer.compare(this.minutesFromMidnight, other.minutesFromMidnight);
    }
}
