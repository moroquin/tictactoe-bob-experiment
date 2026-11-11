package enrollment.domain.sections;

import enrollment.domain.campus.ClassroomRef;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Section choice policy (I18, R29, policies.md).
 * Picks the section with the lowest code that has room.
 */
public class SectionChoicePolicy {

    /**
     * Choose the section with the lowest SectionCode among those that have room.
     *
     * @param sections    all sections of the course, in any order
     * @param capacityOf  returns the capacity of the classroom referenced by a section
     * @return the chosen section, or empty if none has room
     */
    public Optional<Section> choose(List<Section> sections, Function<ClassroomRef, Integer> capacityOf) {
        return sections.stream()
                .filter(s -> {
                    Integer cap = capacityOf.apply(s.classroom());
                    return cap != null && s.hasRoom(cap);
                })
                .min(Comparator.comparing(Section::code));
    }
}
