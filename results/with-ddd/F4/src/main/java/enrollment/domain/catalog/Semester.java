package enrollment.domain.catalog;

import enrollment.domain.RuleViolation;

/** Value object: the semester of a course, from 1 to 10 (I13). */
public record Semester(int value) {

    public Semester {
        if (value < 1 || value > 10)
            throw new RuleViolation("semester must be from 1 to 10");
    }

    @Override
    public String toString() { return String.valueOf(value); }
}
