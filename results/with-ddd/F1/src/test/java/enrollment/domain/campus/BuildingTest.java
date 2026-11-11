package enrollment.domain.campus;

import enrollment.domain.RuleViolation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for Building aggregate (I1, I11). */
class BuildingTest {

    @Test
    void I1_duplicate_classroom_number_rejected() {
        Building b = new Building(new BuildingId(1L), "G");
        b.addClassroom(new Classroom(101, 30));
        assertThrows(RuleViolation.class, () -> b.addClassroom(new Classroom(101, 20)));
    }

    @Test
    void I11_classroom_capacity_must_be_positive() {
        assertThrows(RuleViolation.class, () -> new Classroom(101, 0));
        assertThrows(RuleViolation.class, () -> new Classroom(101, -1));
    }

    @Test
    void I1_different_numbers_in_same_building_allowed() {
        Building b = new Building(new BuildingId(1L), "G");
        b.addClassroom(new Classroom(101, 30));
        b.addClassroom(new Classroom(102, 40));
        assertEquals(2, b.classrooms().size());
    }

    @Test
    void classrooms_listed_in_ascending_number() {
        Building b = new Building(new BuildingId(1L), "G");
        b.addClassroom(new Classroom(110, 20));
        b.addClassroom(new Classroom(105, 30));
        b.addClassroom(new Classroom(12, 75));
        var classrooms = b.classrooms();
        assertEquals(12, classrooms.get(0).number());
        assertEquals(105, classrooms.get(1).number());
        assertEquals(110, classrooms.get(2).number());
    }
}
