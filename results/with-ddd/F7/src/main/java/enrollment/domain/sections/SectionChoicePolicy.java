package enrollment.domain.sections;

import enrollment.domain.campus.ClassroomRef;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Section choice policy (I18, I35, CR-3, CR-6, policies.md).
 * Picks the section with the fewest enrollments that has room and does not overlap
 * any section the student is already enrolled in; ties are broken by the lowest section code.
 */
public class SectionChoicePolicy {

    /**
     * Choose the section with the fewest enrollments (then lowest code) among those that have
     * room and do not overlap any section in {@code enrolledSections} (on the same day).
     *
     * @param sections         all sections of the course, in any order
     * @param capacityOf       returns the capacity of the classroom referenced by a section
     * @param enrolledSections the sections the student is already enrolled in
     * @return the chosen section, or empty if none qualifies
     */
    public Optional<Section> choose(List<Section> sections,
                                    Function<ClassroomRef, Integer> capacityOf,
                                    List<Section> enrolledSections) {
        return sections.stream()
                .filter(s -> {
                    Integer cap = capacityOf.apply(s.classroom());
                    if (cap == null || !s.hasRoom(cap)) return false;
                    return enrolledSections.stream()
                            .noneMatch(e -> e.day().equals(s.day()) && e.period().overlaps(s.period()));
                })
                .min(Comparator
                        .comparingInt((Section s) -> s.enrollments().size())
                        .thenComparing(Section::code));
    }
}
