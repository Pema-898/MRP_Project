package at.technikum.application.testutil;

import at.technikum.application.mrp.user.User;
import at.technikum.application.mrp.user.UserRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryUserRepository implements UserRepository {
    private final Map<Integer, User> users = new LinkedHashMap<>();
    private int nextId = 1;

    public void put(User user) {
        users.put(user.id(), user);
        nextId = Math.max(nextId, user.id() + 1);
    }

    @Override
    public Optional<User> findById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return users.values().stream().filter(u -> u.username().equalsIgnoreCase(username)).findFirst();
    }

    @Override
    public User create(String username, String passwordHash) {
        User user = new User(nextId++, username, passwordHash, null, null);
        users.put(user.id(), user);
        return user;
    }

    @Override
    public User updateProfile(int userId, String email, String favoriteGenre) {
        User old = users.get(userId);
        User updated = new User(old.id(), old.username(), old.passwordHash(), email, favoriteGenre);
        users.put(userId, updated);
        return updated;
    }
}
