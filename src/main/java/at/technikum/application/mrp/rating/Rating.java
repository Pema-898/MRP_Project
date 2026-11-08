package at.technikum.application.mrp.rating;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public record Rating(
        int id,
        int userId,
        String username,
        int mediaId,
        int stars,
        String comment,
        LocalDateTime timestamp,
        boolean confirmed,
        int likeCount
) {
    public Map<String, Object> toMap(boolean includeHiddenComment) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("userId", userId);
        map.put("username", username);
        map.put("mediaId", mediaId);
        map.put("stars", stars);
        map.put("comment", confirmed || includeHiddenComment ? comment : null);
        map.put("timestamp", timestamp.toString());
        map.put("confirmed", confirmed);
        map.put("likeCount", likeCount);
        return map;
    }
}
