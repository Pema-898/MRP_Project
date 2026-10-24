package at.technikum.application.mrp.auth;

import at.technikum.application.server.util.Json;

import java.util.Map;

public record Credentials(String username, String password) {
    public static Credentials from(Map<String, Object> body) {
        return new Credentials(
                Json.requiredString(body, "username"),
                Json.requiredString(body, "password")
        );
    }
}
