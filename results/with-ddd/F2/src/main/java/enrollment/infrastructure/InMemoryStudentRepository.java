package enrollment.infrastructure;

import enrollment.domain.people.Student;
import enrollment.domain.people.StudentNumber;
import enrollment.domain.people.StudentRepository;

import java.util.*;

/** In-memory StudentRepository. Listed in ascending student number order. */
public class InMemoryStudentRepository implements StudentRepository {

    private final Map<StudentNumber, Student> students = new LinkedHashMap<>();

    @Override
    public void add(Student student) {
        students.put(student.number(), student);
    }

    @Override
    public Optional<Student> findByNumber(StudentNumber number) {
        return Optional.ofNullable(students.get(number));
    }

    @Override
    public List<Student> listAll() {
        List<Student> list = new ArrayList<>(students.values());
        list.sort(Comparator.comparingLong(s -> s.number().value()));
        return list;
    }

    @Override
    public void remove(StudentNumber number) {
        students.remove(number);
    }
}
