package enrollment.application;

import enrollment.domain.RuleViolation;
import enrollment.domain.access.Action;
import enrollment.domain.access.UserRepository;
import enrollment.domain.people.*;
import enrollment.domain.sections.SectionRepository;

import java.util.List;

/** Application service for Student and Professor use cases. */
public class PeopleService {

    private final AccessService access;
    private final StudentRepository students;
    private final ProfessorRepository professors;
    private final UserRepository users;
    private final SectionRepository sections;

    public PeopleService(AccessService access, StudentRepository students,
                         ProfessorRepository professors, UserRepository users,
                         SectionRepository sections) {
        this.access = access;
        this.students = students;
        this.professors = professors;
        this.users = users;
        this.sections = sections;
    }

    // ── Students ──────────────────────────────────────────────────────────────

    public Student createStudent(long number, String name, String address) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        StudentNumber sn = new StudentNumber(number);
        if (students.findByNumber(sn).isPresent())
            throw new RuleViolation("student " + number + " already exists");
        Student s = new Student(sn, name, address);
        students.add(s);
        return s;
    }

    public Student modifyStudent(long number, String name, String address) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        Student s = requireStudent(number);
        s.modify(name, address);
        return s;
    }

    public void deleteStudent(long number) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        StudentNumber sn = new StudentNumber(number);
        requireStudent(number);
        // Must have no enrollments (I2)
        if (!sections.enrollmentsOf(sn).isEmpty())
            throw new RuleViolation("student " + number + " has enrollments");
        // Must have no linked user (I2)
        boolean hasUser = users.listAll().stream()
                .anyMatch(u -> u.studentNumber().isPresent() && u.studentNumber().get().equals(sn));
        if (hasUser)
            throw new RuleViolation("student " + number + " has a linked user");
        students.remove(sn);
    }

    public Student queryStudent(long number) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        return requireStudent(number);
    }

    public List<Student> listStudents() {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        return students.listAll();
    }

    // ── Professors ────────────────────────────────────────────────────────────

    public Professor createProfessor(long id, String name, String address) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        ProfessorId pid = new ProfessorId(id);
        if (professors.findById(pid).isPresent())
            throw new RuleViolation("professor " + id + " already exists");
        Professor p = new Professor(pid, name, address);
        professors.add(p);
        return p;
    }

    public Professor modifyProfessor(long id, String name, String address) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        Professor p = requireProfessor(id);
        p.modify(name, address);
        return p;
    }

    public void deleteProfessor(long id) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        ProfessorId pid = new ProfessorId(id);
        requireProfessor(id);
        if (!sections.usingProfessor(pid).isEmpty())
            throw new RuleViolation("professor " + id + " is used by a section");
        professors.remove(pid);
    }

    public Professor queryProfessor(long id) {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        return requireProfessor(id);
    }

    public List<Professor> listProfessors() {
        access.requireSession();
        access.requirePermission(Action.MANAGE_SCHOOL_DATA);
        return professors.listAll();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public Student requireStudent(long number) {
        StudentNumber sn = new StudentNumber(number);
        return students.findByNumber(sn)
                .orElseThrow(() -> new RuleViolation("student " + number + " does not exist"));
    }

    public Professor requireProfessor(long id) {
        ProfessorId pid = new ProfessorId(id);
        return professors.findById(pid)
                .orElseThrow(() -> new RuleViolation("professor " + id + " does not exist"));
    }
}
