package enrollment.domain.people;

import java.util.Objects;

/** Value object: identifies a Professor. */
public record ProfessorId(long value) {

    public ProfessorId {
        if (value <= 0) throw new enrollment.domain.RuleViolation("professor ID must be positive");
    }

    @Override
    public String toString() { return String.valueOf(value); }
}
