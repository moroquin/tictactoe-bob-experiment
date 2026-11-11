package enrollment.domain.people;

import java.util.List;
import java.util.Optional;

/** Repository for Professors. */
public interface ProfessorRepository {

    void add(Professor professor);

    Optional<Professor> findById(ProfessorId id);

    /** All professors in ascending professor ID order. */
    List<Professor> listAll();

    void remove(ProfessorId id);
}
