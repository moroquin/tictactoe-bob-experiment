package enrollment.infrastructure;

import enrollment.domain.campus.Building;
import enrollment.domain.campus.BuildingId;
import enrollment.domain.campus.BuildingRepository;

import java.util.*;

/** In-memory BuildingRepository. Buildings are listed in creation order. */
public class InMemoryBuildingRepository implements BuildingRepository {

    private final List<Building> buildings = new ArrayList<>();
    private long nextIdValue = 1;

    @Override
    public BuildingId nextId() {
        return new BuildingId(nextIdValue++);
    }

    @Override
    public void add(Building building) {
        buildings.add(building);
    }

    @Override
    public Optional<Building> findByName(String name) {
        return buildings.stream().filter(b -> b.name().equals(name)).findFirst();
    }

    @Override
    public Optional<Building> findById(BuildingId id) {
        return buildings.stream().filter(b -> b.id().equals(id)).findFirst();
    }

    @Override
    public List<Building> listAll() {
        return Collections.unmodifiableList(buildings);
    }

    @Override
    public void remove(BuildingId id) {
        buildings.removeIf(b -> b.id().equals(id));
    }
}
