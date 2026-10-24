package at.technikum.application.mrp.media;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record Media(
        int id,
        String title,
        String description,
        MediaType mediaType,
        int releaseYear,
        List<String> genres,
        int ageRestriction,
        int creatorId,
        String creatorUsername,
        double averageScore,
        int favoriteCount
) {
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("description", description);
        map.put("mediaType", mediaType.value());
        map.put("releaseYear", releaseYear);
        map.put("genres", genres);
        map.put("ageRestriction", ageRestriction);
        map.put("creatorId", creatorId);
        map.put("creatorUsername", creatorUsername);
        map.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
        map.put("favoriteCount", favoriteCount);
        return map;
    }
}
