package enrollment.domain.campus;

import java.util.List;
import java.util.Optional;

/** Repository for Buildings. */
public interface BuildingRepository {

    void add(Building building);

    Optional<Building> findByName(String name);

    Optional<Building> findById(BuildingId id);

    /** All buildings in creation order. */
    List<Building> listAll();

    void remove(BuildingId id);

    /** Allocate the next BuildingId. */
    BuildingId nextId();
}
