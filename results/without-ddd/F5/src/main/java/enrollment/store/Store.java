package enrollment.store;

import enrollment.model.*;

import java.util.*;

/**
 * In-memory data store. Holds all entities and provides access by key.
 * Lists are kept in insertion order; sorting for display is done in the service.
 */
public class Store {

    // Users indexed by ID; insertion order for list
    private final Map<Integer, User> usersById = new LinkedHashMap<>();
    private int nextUserId = 2;

    // Buildings by name, in insertion order
    private final Map<String, Building> buildingsByName = new LinkedHashMap<>();

    // Courses by code, in insertion order
    private final Map<Long, Course> coursesByCode = new LinkedHashMap<>();

    // Students by studentNumber, in insertion order
    private final Map<Long, Student> studentsByNumber = new LinkedHashMap<>();

    // Professors by professorId, in insertion order
    private final Map<Long, Professor> professorsById = new LinkedHashMap<>();

    // Sections by code, in insertion order
    private final Map<Long, Section> sectionsByCode = new LinkedHashMap<>();

    public Store() {
        // R5: initial admin user
        usersById.put(1, new User(1, "admin", "admin", UserType.ADMIN, null));
    }

    // ---- Users ----

    public User getUserById(int id) { return usersById.get(id); }

    public User getUserByName(String name) {
        return usersById.values().stream()
                .filter(u -> u.getName().equals(name))
                .findFirst().orElse(null);
    }

    public Collection<User> allUsers() { return usersById.values(); }

    public int addUser(String name, String password, UserType type, Long studentNumber) {
        int id = nextUserId++;
        usersById.put(id, new User(id, name, password, type, studentNumber));
        return id;
    }

    public void removeUser(int id) { usersById.remove(id); }

    // ---- Buildings ----

    public Building getBuildingByName(String name) { return buildingsByName.get(name); }
    public Collection<Building> allBuildings() { return buildingsByName.values(); }

    public void addBuilding(String name) {
        buildingsByName.put(name, new Building(name));
    }

    public void renameBuilding(Building b, String newName) {
        buildingsByName.remove(b.getName());
        b.setName(newName);
        buildingsByName.put(newName, b);
    }

    public void removeBuilding(String name) { buildingsByName.remove(name); }

    // ---- Classrooms ----

    public Classroom getClassroom(Building building, long number) {
        return building.getClassrooms().stream()
                .filter(c -> c.getNumber() == number)
                .findFirst().orElse(null);
    }

    public void addClassroom(Building building, long number, int capacity) {
        building.getClassrooms().add(new Classroom(building, number, capacity));
    }

    public void removeClassroom(Building building, long number) {
        building.getClassrooms().removeIf(c -> c.getNumber() == number);
    }

    // ---- Courses ----

    public Course getCourseByCode(long code) { return coursesByCode.get(code); }
    public Collection<Course> allCourses() { return coursesByCode.values(); }

    public void addCourse(long code, String name, int semester, int credits) {
        coursesByCode.put(code, new Course(code, name, semester, credits));
    }

    public void removeCourse(long code) { coursesByCode.remove(code); }

    // ---- Students ----

    public Student getStudentByNumber(long number) { return studentsByNumber.get(number); }
    public Collection<Student> allStudents() { return studentsByNumber.values(); }

    public void addStudent(long number, String name, String address) {
        studentsByNumber.put(number, new Student(number, name, address));
    }

    public void removeStudent(long number) { studentsByNumber.remove(number); }

    // ---- Professors ----

    public Professor getProfessorById(long id) { return professorsById.get(id); }
    public Collection<Professor> allProfessors() { return professorsById.values(); }

    public void addProfessor(long id, String name, String address) {
        professorsById.put(id, new Professor(id, name, address));
    }

    public void removeProfessor(long id) { professorsById.remove(id); }

    // ---- Sections ----

    public Section getSectionByCode(long code) { return sectionsByCode.get(code); }
    public Collection<Section> allSections() { return sectionsByCode.values(); }

    public void addSection(long code, String period, String day, Course course,
                           Classroom classroom, Professor professor) {
        sectionsByCode.put(code, new Section(code, period, day, course, classroom, professor));
    }

    public void removeSection(long code) { sectionsByCode.remove(code); }

    // ---- Enrollments (managed via sections) ----

    /**
     * Finds the enrollment for a student in any section of the given course.
     */
    public Enrollment findEnrollment(long studentNumber, long courseCode) {
        return sectionsByCode.values().stream()
                .filter(s -> s.getCourse().getCode() == courseCode)
                .flatMap(s -> s.getEnrollments().stream())
                .filter(e -> e.getStudent().getStudentNumber() == studentNumber)
                .findFirst().orElse(null);
    }
}
