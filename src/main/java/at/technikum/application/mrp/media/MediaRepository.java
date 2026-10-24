package at.technikum.application.mrp.media;

import java.util.List;
import java.util.Optional;

public interface MediaRepository {
    Optional<Media> findById(int id);
    List<Media> findAll();
    Media create(MediaInput input, int creatorId);
    boolean update(int mediaId, MediaInput input);
    boolean delete(int mediaId);
}
