package enrollment.domain.sections;

/** Value object: identifies a Section. */
public record SectionCode(long value) implements Comparable<SectionCode> {

    @Override
    public int compareTo(SectionCode other) {
        return Long.compare(this.value, other.value);
    }

    @Override
    public String toString() { return String.valueOf(value); }
}
