package at.technikum.application.server.http;

import at.technikum.application.server.util.Json;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Response {
    private final Status status;
    private final String body;
    private final Map<String, String> headers;

    public Response(Status status, String body, Map<String, String> headers) {
        this.status = status;
        this.body = body == null ? "" : body;
        this.headers = new LinkedHashMap<>(headers);
    }

    public static Response json(Status status, Object body) {
        return new Response(status, Json.stringify(body), Map.of("Content-Type", ContentType.JSON));
    }

    public static Response noContent() {
        return new Response(Status.NO_CONTENT, "", Map.of());
    }

    public Status status() {
        return status;
    }

    public String body() {
        return body;
    }

    public Map<String, String> headers() {
        return Map.copyOf(headers);
    }
}
