package enrollment.domain;

/** Signals a broken domain rule. The reason is a human-readable message. */
public class RuleViolation extends RuntimeException {

    public RuleViolation(String reason) {
        super(reason);
    }

    public String reason() {
        return getMessage();
    }
}
