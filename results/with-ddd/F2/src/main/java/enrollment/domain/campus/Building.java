package enrollment.domain.campus;

import enrollment.domain.RuleViolation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Building aggregate root. Holds its Classrooms.
 * The name can change (R18); identity is BuildingId (D3).
 */
public class Building {

    private final BuildingId id;
    private String name;
    private final List<Classroom> classrooms = new ArrayList<>();

    public Building(BuildingId id, String name) {
        this.id = id;
        this.name = name;
    }

    public BuildingId id() { return id; }
    public String name() { return name; }

    /** Rename. Uniqueness is enforced by CampusService (I12). */
    public void rename(String newName) {
        this.name = newName;
    }

    /** Classrooms in ascending number order. */
    public List<Classroom> classrooms() {
        List<Classroom> sorted = new ArrayList<>(classrooms);
        sorted.sort((a, b) -> Integer.compare(a.number(), b.number()));
        return Collections.unmodifiableList(sorted);
    }

    /** Add a classroom. Unique number within building is enforced here (I1). */
    public void addClassroom(Classroom classroom) {
        for (Classroom c : classrooms) {
            if (c.number() == classroom.number())
                throw new RuleViolation("classroom " + classroom.number() + " already exists in building " + name);
        }
        classrooms.add(classroom);
    }

    /** Find a classroom by number, or null. */
    public Classroom findClassroom(int number) {
        return classrooms.stream().filter(c -> c.number() == number).findFirst().orElse(null);
    }

    /** Remove a classroom by number. Existence checked by caller. */
    public void removeClassroom(int number) {
        classrooms.removeIf(c -> c.number() == number);
    }
}
