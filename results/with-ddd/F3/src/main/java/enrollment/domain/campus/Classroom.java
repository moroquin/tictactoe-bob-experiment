package enrollment.domain.campus;

import enrollment.domain.RuleViolation;

/**
 * A classroom inside a building. Entity (identified by number within its building).
 * Capacity must be greater than 0 (I11).
 */
public class Classroom {

    private final int number;
    private int capacity;

    public Classroom(int number, int capacity) {
        if (capacity <= 0) throw new RuleViolation("capacity must be greater than 0");
        this.number = number;
        this.capacity = capacity;
    }

    public int number() { return number; }
    public int capacity() { return capacity; }

    /** Change the capacity (I11: must stay >= maxEnrollmentsInAnySection, checked by CampusService). */
    public void changeCapacity(int capacity) {
        if (capacity <= 0) throw new RuleViolation("capacity must be greater than 0");
        this.capacity = capacity;
    }
}
