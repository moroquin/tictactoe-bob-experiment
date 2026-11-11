package enrollment.domain.access;

import java.util.List;
import java.util.Optional;

/** Repository for Users (I1, I7). */
public interface UserRepository {

    /** The next ID to assign: one more than the highest ID assigned so far (R11). */
    long nextId();

    void add(User user);

    Optional<User> findById(long id);

    Optional<User> findByName(String name);

    /** All users in ascending ID order (section 3). */
    List<User> listAll();

    void remove(long id);
}
