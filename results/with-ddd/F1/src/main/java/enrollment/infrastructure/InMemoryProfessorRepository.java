package enrollment.infrastructure;

import enrollment.domain.people.Professor;
import enrollment.domain.people.ProfessorId;
import enrollment.domain.people.ProfessorRepository;

import java.util.*;

/** In-memory ProfessorRepository. Listed in ascending professor ID order. */
public class InMemoryProfessorRepository implements ProfessorRepository {

    private final Map<ProfessorId, Professor> professors = new LinkedHashMap<>();

    @Override
    public void add(Professor professor) {
        professors.put(professor.id(), professor);
    }

    @Override
    public Optional<Professor> findById(ProfessorId id) {
        return Optional.ofNullable(professors.get(id));
    }

    @Override
    public List<Professor> listAll() {
        List<Professor> list = new ArrayList<>(professors.values());
        list.sort(Comparator.comparingLong(p -> p.id().value()));
        return list;
    }

    @Override
    public void remove(ProfessorId id) {
        professors.remove(id);
    }
}
