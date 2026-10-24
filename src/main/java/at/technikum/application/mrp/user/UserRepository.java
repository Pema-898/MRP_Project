package at.technikum.application.mrp.user;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(int id);
    Optional<User> findByUsername(String username);
    User create(String username, String passwordHash);
    User updateProfile(int userId, String email, String favoriteGenre);
}
