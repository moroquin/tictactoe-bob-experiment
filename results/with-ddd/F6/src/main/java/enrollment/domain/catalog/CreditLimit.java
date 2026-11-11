package enrollment.domain.catalog;

/**
 * The maximum number of Credits a Student may hold across all their active Enrollments (CR-5, I32).
 */
public final class CreditLimit {

    /** No Student's total credits may exceed this value. */
    public static final int MAX = 24;

    private CreditLimit() {}
}
