package enrollment.application;

import enrollment.domain.RuleViolation;
import enrollment.domain.access.Action;
import enrollment.domain.campus.*;
import enrollment.domain.sections.SectionRepository;

import java.util.List;

/**
 * Application service for Building and Classroom use cases.
 */
public class CampusService {

    private final AccessService access;
    private final BuildingRepository buildings;
    private final SectionRepository sections;

    public CampusService(AccessService access, BuildingRepository buildings,
                         SectionRepository sections) {
        this.access = access;
        this.buildings = buildings;
        this.sections = sections;
    }

    // ── Buildings ─────────────────────────────────────────────────────────────

    public Building createBuilding(String name) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        if (buildings.findByName(name).isPresent())
            throw new RuleViolation("building " + name + " already exists");
        BuildingId id = buildings.nextId();
        Building b = new Building(id, name);
        buildings.add(b);
        return b;
    }

    public Building modifyBuilding(String name, String newName) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        Building b = requireBuildingByName(name);
        if (!name.equals(newName) && buildings.findByName(newName).isPresent())
            throw new RuleViolation("building " + newName + " already exists");
        b.rename(newName);
        return b;
    }

    public void deleteBuilding(String name) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        Building b = requireBuildingByName(name);
        if (!b.classrooms().isEmpty())
            throw new RuleViolation("building " + name + " has classrooms");
        buildings.remove(b.id());
    }

    public Building queryBuilding(String name) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        return requireBuildingByName(name);
    }

    public List<Building> listBuildings() {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        return buildings.listAll();
    }

    // ── Classrooms ────────────────────────────────────────────────────────────

    public Classroom createClassroom(String buildingName, int number, int capacity) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        Building b = requireBuildingByName(buildingName);
        // addClassroom enforces unique number within building (I1)
        Classroom c = new Classroom(number, capacity);
        b.addClassroom(c);
        return c;
    }

    public Classroom modifyClassroom(String buildingName, int number, int newCapacity) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        Building b = requireBuildingByName(buildingName);
        Classroom c = requireClassroom(b, number);
        // Capacity must not fall below the enrollment count of any section using it (I11)
        ClassroomRef ref = new ClassroomRef(b.id(), number);
        int maxEnrolled = sections.usingClassroom(ref).stream()
                .mapToInt(s -> s.enrollmentCount())
                .max().orElse(0);
        if (newCapacity < maxEnrolled)
            throw new RuleViolation("capacity cannot fall below the number of students enrolled");
        c.changeCapacity(newCapacity);
        return c;
    }

    public void deleteClassroom(String buildingName, int number) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        Building b = requireBuildingByName(buildingName);
        requireClassroom(b, number);
        ClassroomRef ref = new ClassroomRef(b.id(), number);
        if (!sections.usingClassroom(ref).isEmpty())
            throw new RuleViolation("classroom " + number + " in building " + buildingName + " is used by a section");
        b.removeClassroom(number);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public Building requireBuildingByName(String name) {
        return buildings.findByName(name)
                .orElseThrow(() -> new RuleViolation("building " + name + " does not exist"));
    }

    public Building requireBuildingById(BuildingId id) {
        return buildings.findById(id)
                .orElseThrow(() -> new RuleViolation("building does not exist"));
    }

    private Classroom requireClassroom(Building b, int number) {
        Classroom c = b.findClassroom(number);
        if (c == null) throw new RuleViolation("classroom " + number + " does not exist in building " + b.name());
        return c;
    }

    /** Look up a classroom's capacity given a ClassroomRef (used by SectionChoicePolicy). */
    public int capacityOf(ClassroomRef ref) {
        return buildings.findById(ref.buildingId())
                .map(b -> {
                    Classroom c = b.findClassroom(ref.number());
                    return c == null ? 0 : c.capacity();
                })
                .orElse(0);
    }
}
