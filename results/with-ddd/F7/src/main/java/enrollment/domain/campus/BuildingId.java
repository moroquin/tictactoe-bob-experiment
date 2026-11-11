package enrollment.domain.campus;

import java.util.Objects;

/** Value object: the immutable internal identity of a Building (D3). */
public record BuildingId(long value) {
    @Override
    public String toString() { return String.valueOf(value); }
}
