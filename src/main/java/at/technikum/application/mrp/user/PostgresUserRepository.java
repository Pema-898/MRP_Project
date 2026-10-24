package at.technikum.application.mrp.user;

import at.technikum.application.common.database.ConnectionFactory;
import at.technikum.application.common.exception.PersistenceException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class PostgresUserRepository implements UserRepository {
    private final ConnectionFactory connectionFactory;

    public PostgresUserRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<User> findById(int id) {
        return find("SELECT id, username, password_hash, email, favorite_genre FROM users WHERE id = ?", id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try (var connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, username, password_hash, email, favorite_genre FROM users WHERE lower(username) = lower(?)")) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new PersistenceException("Could not query user", ex);
        }
    }

    @Override
    public User create(String username, String passwordHash) {
        String sql = "INSERT INTO users(username, password_hash) VALUES (?, ?)";
        try (var connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No generated user id");
                return findById(keys.getInt(1)).orElseThrow();
            }
        } catch (SQLException ex) {
            throw new PersistenceException("Could not create user", ex);
        }
    }

    @Override
    public User updateProfile(int userId, String email, String favoriteGenre) {
        String sql = "UPDATE users SET email = ?, favorite_genre = ? WHERE id = ?";
        try (var connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setString(2, favoriteGenre);
            statement.setInt(3, userId);
            statement.executeUpdate();
            return findById(userId).orElseThrow();
        } catch (SQLException ex) {
            throw new PersistenceException("Could not update user profile", ex);
        }
    }

    private Optional<User> find(String sql, int id) {
        try (var connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new PersistenceException("Could not query user", ex);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("email"),
                rs.getString("favorite_genre")
        );
    }
}
