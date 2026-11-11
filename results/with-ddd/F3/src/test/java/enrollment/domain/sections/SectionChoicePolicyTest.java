package enrollment.domain.sections;

import enrollment.domain.campus.BuildingId;
import enrollment.domain.campus.ClassroomRef;
import enrollment.domain.catalog.CourseCode;
import enrollment.domain.people.ProfessorId;
import enrollment.domain.people.StudentNumber;
import enrollment.domain.RuleViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for SectionChoicePolicy (I18, R29). */
class SectionChoicePolicyTest {

    private final SectionChoicePolicy policy = new SectionChoicePolicy();
    private final GradeScale scale = new GradeScale();
    private final CourseCode course = new CourseCode(1L);
    private final ClassroomRef room60 = new ClassroomRef(new BuildingId(1L), 101);
    private final ClassroomRef room2  = new ClassroomRef(new BuildingId(1L), 102);
    private final ProfessorId prof = new ProfessorId(1L);

    private Section makeSection(long code, ClassroomRef ref) {
        return new Section(new SectionCode(code), Period.parse("7:00am-8:00am"),
                Day.MONDAY, course, ref, prof);
    }

    @Test
    void I18_picks_lowest_code_with_room() {
        Section s1 = makeSection(1L, room60);
        Section s2 = makeSection(2L, room60);
        // capacity 60, no enrollments -> both have room; s1 has lower code
        Optional<Section> chosen = policy.choose(List.of(s2, s1), ref -> 60);
        assertTrue(chosen.isPresent());
        assertEquals(new SectionCode(1L), chosen.get().code());
    }

    @Test
    void I18_skips_full_sections() {
        Section s1 = makeSection(1L, room2);
        Section s2 = makeSection(2L, room60);
        // Fill s1 (capacity 2)
        s1.enroll(new StudentNumber(100L), scale.grades(0, 0), 2);
        s1.enroll(new StudentNumber(101L), scale.grades(0, 0), 2);
        Optional<Section> chosen = policy.choose(List.of(s1, s2),
                ref -> ref.equals(room2) ? 2 : 60);
        assertTrue(chosen.isPresent());
        assertEquals(new SectionCode(2L), chosen.get().code());
    }

    @Test
    void I18_empty_returns_empty() {
        Optional<Section> chosen = policy.choose(List.of(), ref -> 60);
        assertTrue(chosen.isEmpty());
    }

    @Test
    void I18_all_full_returns_empty() {
        Section s1 = makeSection(1L, room2);
        s1.enroll(new StudentNumber(100L), scale.grades(0, 0), 2);
        s1.enroll(new StudentNumber(101L), scale.grades(0, 0), 2);
        Optional<Section> chosen = policy.choose(List.of(s1), ref -> 2);
        assertTrue(chosen.isEmpty());
    }
}
