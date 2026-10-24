package at.technikum.application.testutil;

import at.technikum.application.mrp.media.Media;
import at.technikum.application.mrp.media.MediaInput;
import at.technikum.application.mrp.media.MediaRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryMediaRepository implements MediaRepository {
    private final Map<Integer, Media> media = new LinkedHashMap<>();
    private int nextId = 1;

    public void put(Media item) {
        media.put(item.id(), item);
        nextId = Math.max(nextId, item.id() + 1);
    }

    @Override
    public Optional<Media> findById(int id) {
        return Optional.ofNullable(media.get(id));
    }

    @Override
    public List<Media> findAll() {
        return new ArrayList<>(media.values());
    }

    @Override
    public Media create(MediaInput input, int creatorId) {
        Media item = new Media(nextId++, input.title(), input.description(), input.mediaType(), input.releaseYear(),
                input.genres(), input.ageRestriction(), creatorId, "creator" + creatorId, 0.0, 0);
        media.put(item.id(), item);
        return item;
    }

    @Override
    public boolean update(int mediaId, MediaInput input) {
        Media old = media.get(mediaId);
        if (old == null) return false;
        media.put(mediaId, new Media(old.id(), input.title(), input.description(), input.mediaType(), input.releaseYear(),
                input.genres(), input.ageRestriction(), old.creatorId(), old.creatorUsername(), old.averageScore(), old.favoriteCount()));
        return true;
    }

    @Override
    public boolean delete(int mediaId) {
        return media.remove(mediaId) != null;
    }
}
