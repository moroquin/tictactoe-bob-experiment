package enrollment.domain.people;

import java.util.List;
import java.util.Optional;

/** Repository for Students. */
public interface StudentRepository {

    void add(Student student);

    Optional<Student> findByNumber(StudentNumber number);

    /** All students in ascending student number order. */
    List<Student> listAll();

    void remove(StudentNumber number);
}
