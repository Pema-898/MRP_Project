package at.technikum.application.server.http;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record Request(
        Method method,
        String path,
        Map<String, String> queryParams,
        Map<String, List<String>> headers,
        String body
) {
    public Optional<String> header(String name) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name) && !entry.getValue().isEmpty()) {
                return Optional.ofNullable(entry.getValue().getFirst());
            }
        }
        return Optional.empty();
    }
}
