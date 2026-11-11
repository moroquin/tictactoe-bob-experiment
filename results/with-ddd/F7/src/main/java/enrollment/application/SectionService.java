package enrollment.application;

import enrollment.domain.RuleViolation;
import enrollment.domain.access.Action;
import enrollment.domain.campus.*;
import enrollment.domain.catalog.CourseCode;
import enrollment.domain.people.ProfessorId;
import enrollment.domain.people.StudentNumber;
import enrollment.domain.sections.*;

import java.util.List;

/** Application service for Section use cases. */
public class SectionService {

    private final AccessService access;
    private final CampusService campus;
    private final CatalogService catalog;
    private final PeopleService people;
    private final SectionRepository sections;

    public SectionService(AccessService access, CampusService campus,
                          CatalogService catalog, PeopleService people,
                          SectionRepository sections) {
        this.access = access;
        this.campus = campus;
        this.catalog = catalog;
        this.people = people;
        this.sections = sections;
    }

    /**
     * Create a section (I16).
     * Section(code, period, day, courseCode, classroomNumber, building, professorId)
     */
    public Section createSection(long code, String period, String day,
                                 long courseCode, int classroomNumber,
                                 String buildingName, long professorId) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SECTIONS);
        SectionCode sc = new SectionCode(code);
        if (sections.findByCode(sc).isPresent())
            throw new RuleViolation("section " + code + " already exists");
        Period p = Period.parse(period);
        Day d = Day.parse(day);
        // Validate course exists (I16)
        CourseCode cc = new CourseCode(courseCode);
        catalog.requireCourse(courseCode);
        // Validate building and classroom exist (I16)
        Building b = campus.requireBuildingByName(buildingName);
        Classroom c = b.findClassroom(classroomNumber);
        if (c == null)
            throw new RuleViolation("classroom " + classroomNumber + " does not exist in building " + buildingName);
        ClassroomRef ref = new ClassroomRef(b.id(), classroomNumber);
        // Validate professor exists (I16)
        ProfessorId pid = new ProfessorId(professorId);
        people.requireProfessor(professorId);
        // I34: classroom must not have an overlapping section on the same day
        for (Section existing : sections.usingClassroom(ref)) {
            if (existing.day().equals(d) && existing.period().overlaps(p))
                throw new RuleViolation("classroom " + classroomNumber + " in " + buildingName
                        + " already has an overlapping section on " + day.toLowerCase());
        }
        // I34: professor must not teach an overlapping section on the same day
        for (Section existing : sections.usingProfessor(pid)) {
            if (existing.day().equals(d) && existing.period().overlaps(p))
                throw new RuleViolation("professor " + professorId
                        + " already teaches an overlapping section on " + day.toLowerCase());
        }
        Section section = new Section(sc, p, d, cc, ref, pid);
        sections.add(section);
        return section;
    }

    /**
     * Modify a section (I22, R26b).
     * ModifySection(code, period, day, courseCode, classroomNumber, building, professorId)
     */
    public Section modifySection(long code, String period, String day,
                                 long courseCode, int classroomNumber,
                                 String buildingName, long professorId) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SECTIONS);
        SectionCode sc = new SectionCode(code);
        Section section = sections.findByCode(sc)
                .orElseThrow(() -> new RuleViolation("section " + code + " does not exist"));
        Period p = Period.parse(period);
        Day d = Day.parse(day);
        CourseCode cc = new CourseCode(courseCode);
        // Validate course exists (I16)
        catalog.requireCourse(courseCode);
        // Validate building and classroom exist (I16)
        Building b = campus.requireBuildingByName(buildingName);
        Classroom c = b.findClassroom(classroomNumber);
        if (c == null)
            throw new RuleViolation("classroom " + classroomNumber + " does not exist in building " + buildingName);
        ClassroomRef ref = new ClassroomRef(b.id(), classroomNumber);
        // Validate professor exists (I16)
        ProfessorId pid = new ProfessorId(professorId);
        people.requireProfessor(professorId);
        // Cross-aggregate rules when the section has enrollments (I22)
        if (!section.enrollments().isEmpty()) {
            if (!section.course().equals(cc))
                throw new RuleViolation("section " + code + " has enrollments; its course cannot change");
            if (c.capacity() < section.enrollmentCount())
                throw new RuleViolation("new classroom capacity is lower than the number of enrolled students");
        }
        // I34: classroom must not have an overlapping section on the same day (excluding this section itself)
        for (Section existing : sections.usingClassroom(ref)) {
            if (existing.code().equals(sc)) continue; // skip self
            if (existing.day().equals(d) && existing.period().overlaps(p))
                throw new RuleViolation("classroom " + classroomNumber + " in " + buildingName
                        + " already has an overlapping section on " + day.toLowerCase());
        }
        // I34: professor must not teach an overlapping section on the same day (excluding this section itself)
        for (Section existing : sections.usingProfessor(pid)) {
            if (existing.code().equals(sc)) continue; // skip self
            if (existing.day().equals(d) && existing.period().overlaps(p))
                throw new RuleViolation("professor " + professorId
                        + " already teaches an overlapping section on " + day.toLowerCase());
        }
        // I36: after the change, no enrolled student may have two overlapping sections
        for (Enrollment enrollment : section.enrollments()) {
            StudentNumber sn = enrollment.student();
            for (Section other : sections.enrolledSections(sn)) {
                if (other.code().equals(sc)) continue; // skip the section being modified
                if (other.day().equals(d) && other.period().overlaps(p))
                    throw new RuleViolation("modifying section " + code
                            + " would create a time conflict for student " + sn.value());
            }
        }
        section.modify(p, d, cc, ref, pid);
        return section;
    }

    /**
     * Delete a section (I23, R26c).
     * DeleteSection(code)
     */
    public void deleteSection(long code) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SECTIONS);
        SectionCode sc = new SectionCode(code);
        Section section = sections.findByCode(sc)
                .orElseThrow(() -> new RuleViolation("section " + code + " does not exist"));
        if (!section.enrollments().isEmpty())
            throw new RuleViolation("section " + code + " has enrollments");
        sections.remove(sc);
    }

    public Section querySection(long code) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SECTIONS);
        SectionCode sc = new SectionCode(code);
        return sections.findByCode(sc)
                .orElseThrow(() -> new RuleViolation("section " + code + " does not exist"));
    }

    public List<Section> listSections() {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SECTIONS);
        return sections.listAll();
    }

    /** Resolve a section's classroom data for formatting. */
    public Classroom classroomOf(Section section) {
        Building b = campus.requireBuildingById(section.classroom().buildingId());
        return b.findClassroom(section.classroom().number());
    }

    /** Resolve a section's building name for formatting. */
    public String buildingNameOf(Section section) {
        return campus.requireBuildingById(section.classroom().buildingId()).name();
    }
}
