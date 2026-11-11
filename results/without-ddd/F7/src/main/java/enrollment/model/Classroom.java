package enrollment.model;

public class Classroom {
    private final Building building;
    private final long number;
    private int capacity;

    public Classroom(Building building, long number, int capacity) {
        this.building = building;
        this.number = number;
        this.capacity = capacity;
    }

    public Building getBuilding() { return building; }
    public long getNumber() { return number; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
}
