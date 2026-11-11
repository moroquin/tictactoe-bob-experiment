package enrollment.application;

import enrollment.domain.RuleViolation;
import enrollment.domain.access.Action;
import enrollment.domain.campus.*;
import enrollment.domain.catalog.CourseCode;
import enrollment.domain.people.ProfessorId;
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
        Section section = new Section(sc, p, d, cc, ref, pid);
        sections.add(section);
        return section;
    }

    /**
     * Modify a section (CR-1, I23).
     * ModifySection(code, period, day, courseCode, classroomNumber, building, professorId)
     */
    public void modifySection(long code, String period, String day,
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
        // If the section has enrollments, the course cannot change (I23)
        if (!section.enrollments().isEmpty() && !section.course().equals(cc))
            throw new RuleViolation("section " + code + " has enrollments; its course cannot change");
        // Validate the course exists (I23 → R26)
        catalog.requireCourse(courseCode);
        // Validate building and classroom exist (I23 → R26)
        Building b = campus.requireBuildingByName(buildingName);
        Classroom c = b.findClassroom(classroomNumber);
        if (c == null)
            throw new RuleViolation("classroom " + classroomNumber + " does not exist in building " + buildingName);
        // Capacity of the new classroom must not be lower than enrolled students (I23)
        int enrolled = section.enrollmentCount();
        if (c.capacity() < enrolled)
            throw new RuleViolation("classroom " + classroomNumber + " in building " + buildingName
                    + " has capacity " + c.capacity() + " but section " + code + " has " + enrolled + " students enrolled");
        ClassroomRef ref = new ClassroomRef(b.id(), classroomNumber);
        // Validate professor exists (I23 → R26)
        ProfessorId pid = new ProfessorId(professorId);
        people.requireProfessor(professorId);
        section.modify(p, d, cc, ref, pid);
    }

    /**
     * Delete a section (CR-1, I22).
     * DeleteSection(code)
     */
    public void deleteSection(long code) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SECTIONS);
        SectionCode sc = new SectionCode(code);
        Section section = sections.findByCode(sc)
                .orElseThrow(() -> new RuleViolation("section " + code + " does not exist"));
        // A section with enrollments cannot be deleted (I22)
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
