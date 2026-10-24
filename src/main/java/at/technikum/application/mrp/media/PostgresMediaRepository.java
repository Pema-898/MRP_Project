package at.technikum.application.mrp.media;

import at.technikum.application.common.database.ConnectionFactory;
import at.technikum.application.common.exception.PersistenceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PostgresMediaRepository implements MediaRepository {
    private static final String SELECT_BASE = """
            SELECT m.id, m.title, m.description, m.media_type, m.release_year, m.age_restriction,
                   m.creator_id, u.username AS creator_username,
                   COALESCE(AVG(r.stars) FILTER (WHERE r.confirmed = TRUE), 0) AS average_score,
                   COUNT(DISTINCT f.user_id) AS favorite_count,
                   COALESCE(string_agg(DISTINCT mg.genre, '|' ORDER BY mg.genre), '') AS genres
              FROM media m
              JOIN users u ON u.id = m.creator_id
              LEFT JOIN media_genres mg ON mg.media_id = m.id
              LEFT JOIN ratings r ON r.media_id = m.id
              LEFT JOIN favorites f ON f.media_id = m.id
            """;
    private static final String GROUP_BY = " GROUP BY m.id, u.username ";

    private final ConnectionFactory connectionFactory;

    public PostgresMediaRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<Media> findById(int id) {
        String sql = SELECT_BASE + " WHERE m.id = ? " + GROUP_BY;
        try (var connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new PersistenceException("Could not find media", ex);
        }
    }

    @Override
    public List<Media> findAll() {
        String sql = SELECT_BASE + GROUP_BY;
        List<Media> result = new ArrayList<>();
        try (var connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) result.add(map(rs));
            return result;
        } catch (SQLException ex) {
            throw new PersistenceException("Could not list media", ex);
        }
    }

    @Override
    public Media create(MediaInput input, int creatorId) {
        try (Connection connection = connectionFactory.open()) {
            connection.setAutoCommit(false);
            try {
                int id;
                String sql = "INSERT INTO media(title, description, media_type, release_year, age_restriction, creator_id) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    bindMedia(statement, input);
                    statement.setInt(6, creatorId);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("No generated media id");
                        id = keys.getInt(1);
                    }
                }
                insertGenres(connection, id, input.genres());
                connection.commit();
                return findById(id).orElseThrow();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new PersistenceException("Could not create media", ex);
        }
    }

    @Override
    public boolean update(int mediaId, MediaInput input) {
        try (Connection connection = connectionFactory.open()) {
            connection.setAutoCommit(false);
            try {
                String sql = "UPDATE media SET title=?, description=?, media_type=?, release_year=?, age_restriction=? WHERE id=?";
                int changed;
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    bindMedia(statement, input);
                    statement.setInt(6, mediaId);
                    changed = statement.executeUpdate();
                }
                if (changed == 0) {
                    connection.rollback();
                    return false;
                }
                try (PreparedStatement deleteGenres = connection.prepareStatement("DELETE FROM media_genres WHERE media_id = ?")) {
                    deleteGenres.setInt(1, mediaId);
                    deleteGenres.executeUpdate();
                }
                insertGenres(connection, mediaId, input.genres());
                connection.commit();
                return true;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new PersistenceException("Could not update media", ex);
        }
    }

    @Override
    public boolean delete(int mediaId) {
        try (var connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM media WHERE id = ?")) {
            statement.setInt(1, mediaId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new PersistenceException("Could not delete media", ex);
        }
    }

    private void bindMedia(PreparedStatement statement, MediaInput input) throws SQLException {
        statement.setString(1, input.title());
        statement.setString(2, input.description());
        statement.setString(3, input.mediaType().value());
        statement.setInt(4, input.releaseYear());
        statement.setInt(5, input.ageRestriction());
    }

    private void insertGenres(Connection connection, int mediaId, List<String> genres) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO media_genres(media_id, genre) VALUES (?, ?)")) {
            for (String genre : genres) {
                statement.setInt(1, mediaId);
                statement.setString(2, genre.trim().toLowerCase());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private Media map(ResultSet rs) throws SQLException {
        String rawGenres = rs.getString("genres");
        List<String> genres = rawGenres == null || rawGenres.isBlank()
                ? List.of()
                : Arrays.stream(rawGenres.split("\\|"))
                .filter(s -> !s.isBlank()).toList();
        return new Media(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                MediaType.from(rs.getString("media_type")),
                rs.getInt("release_year"),
                genres,
                rs.getInt("age_restriction"),
                rs.getInt("creator_id"),
                rs.getString("creator_username"),
                rs.getDouble("average_score"),
                rs.getInt("favorite_count")
        );
    }
}
