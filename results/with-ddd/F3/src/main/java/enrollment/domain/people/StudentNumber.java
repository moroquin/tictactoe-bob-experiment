package enrollment.domain.people;

import java.util.Objects;

/** Value object: identifies a Student. */
public record StudentNumber(long value) {

    public StudentNumber {
        if (value <= 0) throw new enrollment.domain.RuleViolation("student number must be positive");
    }

    @Override
    public String toString() { return String.valueOf(value); }
}
