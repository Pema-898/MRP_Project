package at.technikum.application.mrp.media;

import at.technikum.application.common.exception.BadRequestException;
import at.technikum.application.common.exception.ForbiddenException;
import at.technikum.application.testutil.InMemoryMediaRepository;
import at.technikum.application.testutil.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MediaServiceTest {
    private InMemoryMediaRepository repository;
    private MediaService service;

    @BeforeEach
    void setup() {
        repository = new InMemoryMediaRepository();
        repository.put(TestData.media(1, "Inception", MediaType.MOVIE, List.of("sci-fi", "thriller"), 12, 1, 4.8));
        repository.put(TestData.media(2, "Interstellar", MediaType.MOVIE, List.of("sci-fi", "drama"), 12, 2, 4.6));
        repository.put(TestData.media(3, "Portal", MediaType.GAME, List.of("puzzle", "sci-fi"), 7, 1, 4.2));
        service = new MediaService(repository);
    }

    @Test
    void createMediaAssignsCreator() {
        var input = new MediaInput("Dune", "desc", MediaType.MOVIE, 2021, List.of("Sci-Fi"), 12);
        var created = service.create(TestData.user(9, "owner"), input);
        assertEquals(9, created.creatorId());
        assertEquals(List.of("sci-fi"), created.genres());
    }

    @Test
    void invalidReleaseYearIsRejected() {
        var input = new MediaInput("X", null, MediaType.MOVIE, 0, List.of("x"), 0);
        assertThrows(BadRequestException.class, () -> service.create(TestData.user(1, "u"), input));
    }

    @Test
    void invalidAgeRestrictionIsRejected() {
        var input = new MediaInput("X", null, MediaType.MOVIE, 2020, List.of("x"), 99);
        assertThrows(BadRequestException.class, () -> service.create(TestData.user(1, "u"), input));
    }

    @Test
    void partialTitleFilterIsCaseInsensitive() {
        var result = service.search(MediaFilter.from(Map.of("title", "CEPT")));
        assertEquals(List.of("Inception"), result.stream().map(Media::title).toList());
    }

    @Test
    void genreFilterWorks() {
        var result = service.search(MediaFilter.from(Map.of("genre", "PUZZLE")));
        assertEquals(1, result.size());
        assertEquals("Portal", result.getFirst().title());
    }

    @Test
    void minimumRatingFilterWorks() {
        var result = service.search(MediaFilter.from(Map.of("rating", "4.7")));
        assertEquals(1, result.size());
        assertEquals("Inception", result.getFirst().title());
    }

    @Test
    void scoreSortingWorks() {
        var result = service.search(MediaFilter.from(Map.of("sortBy", "score")));
        assertEquals(List.of("Inception", "Interstellar", "Portal"), result.stream().map(Media::title).toList());
    }

    @Test
    void nonCreatorCannotUpdateMedia() {
        var input = new MediaInput("New", "desc", MediaType.MOVIE, 2020, List.of("x"), 12);
        assertThrows(ForbiddenException.class, () -> service.update(TestData.user(2, "other"), 1, input));
    }

    @Test
    void creatorCanDeleteMedia() {
        service.delete(TestData.user(1, "owner"), 1);
        assertTrue(repository.findById(1).isEmpty());
    }
}
