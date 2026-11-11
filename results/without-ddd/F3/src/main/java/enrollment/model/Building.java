package enrollment.model;

import java.util.ArrayList;
import java.util.List;

public class Building {
    private String name;
    private final List<Classroom> classrooms = new ArrayList<>();

    public Building(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Classroom> getClassrooms() { return classrooms; }
}
