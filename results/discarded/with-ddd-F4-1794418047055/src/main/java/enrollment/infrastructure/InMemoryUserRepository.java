package enrollment.infrastructure;

import enrollment.domain.access.User;
import enrollment.domain.access.UserRepository;

import java.util.*;

/** In-memory UserRepository. IDs are never reused (I7). */
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> byId = new LinkedHashMap<>();
    private long maxIdSoFar = 1; // starts at 1 because the initial admin has ID 1

    @Override
    public long nextId() {
        return maxIdSoFar + 1;
    }

    @Override
    public void add(User user) {
        byId.put(user.id(), user);
        if (user.id() > maxIdSoFar) maxIdSoFar = user.id();
    }

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<User> findByName(String name) {
        return byId.values().stream().filter(u -> u.name().equals(name)).findFirst();
    }

    @Override
    public List<User> listAll() {
        List<User> list = new ArrayList<>(byId.values());
        list.sort(Comparator.comparingLong(User::id));
        return list;
    }

    @Override
    public void remove(long id) {
        byId.remove(id);
    }
}
