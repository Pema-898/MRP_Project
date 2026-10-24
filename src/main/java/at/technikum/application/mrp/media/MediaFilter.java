package at.technikum.application.mrp.media;

import at.technikum.application.common.exception.BadRequestException;

import java.util.Map;

public record MediaFilter(
        String title,
        String genre,
        MediaType mediaType,
        Integer releaseYear,
        Integer ageRestriction,
        Double minimumRating,
        SortBy sortBy
) {
    public enum SortBy { TITLE, YEAR, SCORE }

    public static MediaFilter from(Map<String, String> query) {
        String title = blankToNull(query.get("title"));
        String genre = blankToNull(query.get("genre"));
        MediaType mediaType = query.containsKey("mediaType") && !query.get("mediaType").isBlank()
                ? MediaType.from(query.get("mediaType")) : null;
        Integer year = parseInt(query.get("releaseYear"), "RELEASE_YEAR_INVALID");
        Integer age = parseInt(query.get("ageRestriction"), "AGE_RESTRICTION_INVALID");
        String ratingRaw = firstNonBlank(query.get("rating"), query.get("minimumRating"));
        Double rating = parseDouble(ratingRaw, "RATING_FILTER_INVALID");
        SortBy sort = parseSort(query.get("sortBy"));
        return new MediaFilter(title, genre, mediaType, year, age, rating, sort);
    }

    private static SortBy parseSort(String raw) {
        if (raw == null || raw.isBlank()) return SortBy.TITLE;
        return switch (raw.toLowerCase()) {
            case "title" -> SortBy.TITLE;
            case "year", "releaseyear" -> SortBy.YEAR;
            case "score", "averagescore" -> SortBy.SCORE;
            default -> throw new BadRequestException("SORT_BY_INVALID");
        };
    }

    private static Integer parseInt(String raw, String error) {
        if (raw == null || raw.isBlank()) return null;
        try { return Integer.parseInt(raw); }
        catch (NumberFormatException ex) { throw new BadRequestException(error); }
    }

    private static Double parseDouble(String raw, String error) {
        if (raw == null || raw.isBlank()) return null;
        try { return Double.parseDouble(raw); }
        catch (NumberFormatException ex) { throw new BadRequestException(error); }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static String firstNonBlank(String a, String b) {
        return a != null && !a.isBlank() ? a : b;
    }
}
