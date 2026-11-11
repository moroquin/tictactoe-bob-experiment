package enrollment.domain.campus;

import java.util.Objects;

/**
 * Value object: identifies a Classroom by its Building's immutable ID and its number (D3).
 */
public record ClassroomRef(BuildingId buildingId, int number) {

    @Override
    public String toString() {
        return buildingId + "/" + number;
    }
}
