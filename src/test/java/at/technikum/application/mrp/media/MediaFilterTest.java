package at.technikum.application.mrp.media;

import at.technikum.application.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MediaFilterTest {
    @Test
    void parsesFullFilter() {
        MediaFilter filter = MediaFilter.from(Map.of(
                "title", "incep", "genre", "sci-fi", "mediaType", "movie",
                "releaseYear", "2010", "ageRestriction", "12", "rating", "4.0", "sortBy", "score"));
        assertEquals("incep", filter.title());
        assertEquals(MediaType.MOVIE, filter.mediaType());
        assertEquals(2010, filter.releaseYear());
        assertEquals(4.0, filter.minimumRating());
        assertEquals(MediaFilter.SortBy.SCORE, filter.sortBy());
    }

    @Test
    void defaultsToTitleSort() {
        assertEquals(MediaFilter.SortBy.TITLE, MediaFilter.from(Map.of()).sortBy());
    }

    @Test
    void minimumRatingAliasWorks() {
        assertEquals(3.5, MediaFilter.from(Map.of("minimumRating", "3.5")).minimumRating());
    }

    @Test
    void invalidSortIsRejected() {
        assertThrows(BadRequestException.class, () -> MediaFilter.from(Map.of("sortBy", "banana")));
    }
}
