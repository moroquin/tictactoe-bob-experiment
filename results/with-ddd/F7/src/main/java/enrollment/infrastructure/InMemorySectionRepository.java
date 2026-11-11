package enrollment.infrastructure;

import enrollment.domain.campus.ClassroomRef;
import enrollment.domain.catalog.CourseCode;
import enrollment.domain.people.ProfessorId;
import enrollment.domain.people.StudentNumber;
import enrollment.domain.sections.Enrollment;
import enrollment.domain.sections.Section;
import enrollment.domain.sections.SectionCode;
import enrollment.domain.sections.SectionRepository;

import java.util.*;

/** In-memory SectionRepository. Listed in ascending code order. */
public class InMemorySectionRepository implements SectionRepository {

    private final Map<SectionCode, Section> sections = new LinkedHashMap<>();

    @Override
    public void add(Section section) {
        sections.put(section.code(), section);
    }

    @Override
    public void remove(SectionCode code) {
        sections.remove(code);
    }

    @Override
    public Optional<Section> findByCode(SectionCode code) {
        return Optional.ofNullable(sections.get(code));
    }

    @Override
    public List<Section> listAll() {
        List<Section> list = new ArrayList<>(sections.values());
        list.sort(Comparator.comparing(Section::code));
        return list;
    }

    @Override
    public List<Section> ofCourse(CourseCode courseCode) {
        List<Section> list = new ArrayList<>();
        for (Section s : sections.values()) {
            if (s.course().equals(courseCode)) list.add(s);
        }
        list.sort(Comparator.comparing(Section::code));
        return list;
    }

    @Override
    public List<Section> usingClassroom(ClassroomRef ref) {
        List<Section> list = new ArrayList<>();
        for (Section s : sections.values()) {
            if (s.classroom().equals(ref)) list.add(s);
        }
        return list;
    }

    @Override
    public List<Section> usingProfessor(ProfessorId id) {
        List<Section> list = new ArrayList<>();
        for (Section s : sections.values()) {
            if (s.professor().equals(id)) list.add(s);
        }
        return list;
    }

    @Override
    public List<Enrollment> enrollmentsOf(StudentNumber student) {
        List<Enrollment> result = new ArrayList<>();
        for (Section s : sections.values()) {
            for (Enrollment e : s.enrollments()) {
                if (e.student().equals(student)) result.add(e);
            }
        }
        return result;
    }

    @Override
    public Optional<Section> enrolledSection(StudentNumber student, CourseCode courseCode) {
        for (Section s : sections.values()) {
            if (!s.course().equals(courseCode)) continue;
            for (Enrollment e : s.enrollments()) {
                if (e.student().equals(student)) return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<CourseCode> enrolledCourseCodes(StudentNumber student) {
        List<CourseCode> result = new ArrayList<>();
        for (Section s : sections.values()) {
            for (Enrollment e : s.enrollments()) {
                if (e.student().equals(student)) {
                    result.add(s.course());
                    break; // only one enrollment per course per student (I19)
                }
            }
        }
        return result;
    }

    @Override
    public List<Section> enrolledSections(StudentNumber student) {
        List<Section> result = new ArrayList<>();
        for (Section s : sections.values()) {
            for (Enrollment e : s.enrollments()) {
                if (e.student().equals(student)) {
                    result.add(s);
                    break;
                }
            }
        }
        return result;
    }
}
