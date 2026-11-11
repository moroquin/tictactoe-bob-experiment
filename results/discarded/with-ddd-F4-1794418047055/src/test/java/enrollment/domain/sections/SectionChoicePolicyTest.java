package enrollment.domain.sections;

import enrollment.domain.campus.BuildingId;
import enrollment.domain.campus.ClassroomRef;
import enrollment.domain.catalog.CourseCode;
import enrollment.domain.people.ProfessorId;
import enrollment.domain.people.StudentNumber;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for SectionChoicePolicy (I18, CR-3). */
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
    void I18_picks_fewest_enrollments_tie_broken_by_lowest_code() {
        Section s1 = makeSection(1L, room60);
        Section s2 = makeSection(2L, room60);
        // Both have 0 enrollments; s1 has lower code → s1 chosen
        Optional<Section> chosen = policy.choose(List.of(s2, s1), ref -> 60);
        assertTrue(chosen.isPresent());
        assertEquals(new SectionCode(1L), chosen.get().code());
    }

    @Test
    void I18_picks_section_with_fewer_enrollments_over_lower_code() {
        Section s1 = makeSection(1L, room60);
        Section s2 = makeSection(2L, room60);
        // Enroll one student in s1; s2 has fewer enrollments → s2 chosen
        s1.enroll(new StudentNumber(100L), scale.grades(0, 0), 60);
        Optional<Section> chosen = policy.choose(List.of(s1, s2), ref -> 60);
        assertTrue(chosen.isPresent());
        assertEquals(new SectionCode(2L), chosen.get().code());
    }

    @Test
    void I18_balanced_distribution_three_students() {
        Section s1 = makeSection(1L, room60);
        Section s2 = makeSection(2L, room60);
        // 1st: both empty → s1 (lowest code)
        Optional<Section> c1 = policy.choose(List.of(s1, s2), ref -> 60);
        assertEquals(new SectionCode(1L), c1.get().code());
        s1.enroll(new StudentNumber(100L), scale.grades(0, 0), 60);
        // 2nd: s1 has 1, s2 has 0 → s2
        Optional<Section> c2 = policy.choose(List.of(s1, s2), ref -> 60);
        assertEquals(new SectionCode(2L), c2.get().code());
        s2.enroll(new StudentNumber(101L), scale.grades(0, 0), 60);
        // 3rd: both have 1 → s1 (lowest code)
        Optional<Section> c3 = policy.choose(List.of(s1, s2), ref -> 60);
        assertEquals(new SectionCode(1L), c3.get().code());
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
    void I18_full_section_with_fewer_nominal_enrollments_is_skipped() {
        // s1 has 2 enrollments, capacity 2 (full); s2 has 3 enrollments, capacity 60 (has room)
        // The policy must pick s2 even though s2 has more enrollments, because s1 has no room
        Section s1 = makeSection(1L, room2);
        Section s2 = makeSection(2L, room60);
        s1.enroll(new StudentNumber(100L), scale.grades(0, 0), 2);
        s1.enroll(new StudentNumber(101L), scale.grades(0, 0), 2);
        s2.enroll(new StudentNumber(102L), scale.grades(0, 0), 60);
        s2.enroll(new StudentNumber(103L), scale.grades(0, 0), 60);
        s2.enroll(new StudentNumber(104L), scale.grades(0, 0), 60);
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
