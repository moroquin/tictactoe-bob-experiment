package enrollment.domain.sections;

import enrollment.domain.RuleViolation;

/**
 * Value object: start and end time of a section. The start is before the end (I14, R24).
 * Canonical form: stored in lower case.
 */
public record Period(TimeOfDay start, TimeOfDay end) {

    public Period {
        if (!start.isBefore(end))
            throw new RuleViolation("period start must be before end");
    }

    /**
     * Parse a period string such as "7:10am-8:00am" or "12:00PM-1:00PM".
     */
    public static Period parse(String text) {
        String t = text.strip();
        int dash = findDash(t);
        if (dash < 0) throw new RuleViolation("invalid period: " + text);
        TimeOfDay start = TimeOfDay.parse(t.substring(0, dash));
        TimeOfDay end = TimeOfDay.parse(t.substring(dash + 1));
        return new Period(start, end);
    }

    /** Find the hyphen that separates start and end, skipping am/pm letters. */
    private static int findDash(String t) {
        // The dash separator appears after am/pm of the start; find it after position 4
        for (int i = 4; i < t.length(); i++) {
            if (t.charAt(i) == '-') return i;
        }
        return -1;
    }

    /**
     * True when this period and {@code other} overlap on the same day (I14, CR-6, D30).
     * Two periods overlap when each starts strictly before the other ends.
     * Periods that only touch (end of one equals start of the next) do not overlap.
     */
    public boolean overlaps(Period other) {
        return this.start.isBefore(other.end) && other.start.isBefore(this.end);
    }

    @Override
    public String toString() {
        return start + "-" + end;
    }
}
