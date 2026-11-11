package enrollment.domain.access;

import java.util.Optional;

/**
 * The currently open session. There is at most one open session at a time.
 */
public class Session {

    private User current;

    /** The user who is logged in, or empty if no session is open. */
    public Optional<User> user() {
        return Optional.ofNullable(current);
    }

    public boolean isOpen() {
        return current != null;
    }

    public void open(User user) {
        this.current = user;
    }

    public void close() {
        this.current = null;
    }
}
