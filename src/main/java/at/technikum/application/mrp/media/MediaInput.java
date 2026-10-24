package at.technikum.application.mrp.media;

import at.technikum.application.server.util.Json;

import java.util.List;
import java.util.Map;

public record MediaInput(
        String title,
        String description,
        MediaType mediaType,
        int releaseYear,
        List<String> genres,
        int ageRestriction
) {
    public static MediaInput from(Map<String, Object> body) {
        return new MediaInput(
                Json.requiredString(body, "title"),
                Json.optionalString(body, "description"),
                MediaType.from(Json.requiredString(body, "mediaType")),
                Json.requiredInt(body, "releaseYear"),
                Json.requiredStringList(body, "genres"),
                Json.requiredInt(body, "ageRestriction")
        );
    }
}
