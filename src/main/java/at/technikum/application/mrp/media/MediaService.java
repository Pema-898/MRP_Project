package at.technikum.application.mrp.media;

import at.technikum.application.common.exception.BadRequestException;
import at.technikum.application.common.exception.ForbiddenException;
import at.technikum.application.common.exception.NotFoundException;
import at.technikum.application.mrp.user.User;

import java.time.Year;
import java.util.Comparator;
import java.util.List;

public class MediaService {
    private final MediaRepository media;

    public MediaService(MediaRepository media) {
        this.media = media;
    }

    public Media create(User user, MediaInput input) {
        validate(input);
        return media.create(normalize(input), user.id());
    }

    public Media get(int mediaId) {
        return media.findById(mediaId).orElseThrow(() -> new NotFoundException("MEDIA_NOT_FOUND"));
    }

    public List<Media> search(MediaFilter filter) {
        Comparator<Media> comparator = switch (filter.sortBy()) {
            case TITLE -> Comparator.comparing(Media::title, String.CASE_INSENSITIVE_ORDER);
            case YEAR -> Comparator.comparingInt(Media::releaseYear);
            case SCORE -> Comparator.comparingDouble(Media::averageScore).reversed();
        };

        return media.findAll().stream()
                .filter(m -> filter.title() == null || m.title().toLowerCase().contains(filter.title().toLowerCase()))
                .filter(m -> filter.genre() == null || m.genres().stream().anyMatch(g -> g.equalsIgnoreCase(filter.genre())))
                .filter(m -> filter.mediaType() == null || m.mediaType() == filter.mediaType())
                .filter(m -> filter.releaseYear() == null || m.releaseYear() == filter.releaseYear())
                .filter(m -> filter.ageRestriction() == null || m.ageRestriction() == filter.ageRestriction())
                .filter(m -> filter.minimumRating() == null || m.averageScore() >= filter.minimumRating())
                .sorted(comparator.thenComparingInt(Media::id))
                .toList();
    }

    public Media update(User user, int mediaId, MediaInput input) {
        validate(input);
        Media existing = get(mediaId);
        requireCreator(user, existing);
        if (!media.update(mediaId, normalize(input))) throw new NotFoundException("MEDIA_NOT_FOUND");
        return get(mediaId);
    }

    public void delete(User user, int mediaId) {
        Media existing = get(mediaId);
        requireCreator(user, existing);
        if (!media.delete(mediaId)) throw new NotFoundException("MEDIA_NOT_FOUND");
    }

    public List<Media> all() {
        return media.findAll();
    }

    private void requireCreator(User user, Media existing) {
        if (existing.creatorId() != user.id()) throw new ForbiddenException("MEDIA_NOT_OWNED_BY_USER");
    }

    private void validate(MediaInput input) {
        if (input.title() == null || input.title().isBlank()) throw new BadRequestException("TITLE_REQUIRED");
        if (input.releaseYear() < 1 || input.releaseYear() > Year.now().getValue() + 10) {
            throw new BadRequestException("RELEASE_YEAR_INVALID");
        }
        if (input.ageRestriction() < 0 || input.ageRestriction() > 21) {
            throw new BadRequestException("AGE_RESTRICTION_INVALID");
        }
        if (input.genres() == null || input.genres().isEmpty()) throw new BadRequestException("GENRES_REQUIRED");
    }

    private MediaInput normalize(MediaInput input) {
        List<String> genres = input.genres().stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .distinct()
                .toList();
        if (genres.isEmpty()) throw new BadRequestException("GENRES_REQUIRED");
        return new MediaInput(
                input.title().trim(),
                input.description() == null ? null : input.description().trim(),
                input.mediaType(),
                input.releaseYear(),
                genres,
                input.ageRestriction()
        );
    }
}
