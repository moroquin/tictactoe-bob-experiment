package enrollment.domain.catalog;

/** Value object: identifies a Course. */
public record CourseCode(long value) {
    @Override
    public String toString() { return String.valueOf(value); }
}
