package at.technikum.application.mrp.auth;

import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TokenService {
    private final ConcurrentMap<String, Integer> tokens = new ConcurrentHashMap<>();

    public String issue(String username, int userId) {
        String token = username + "-mrpToken";
        tokens.put(token, userId);
        return token;
    }

    public OptionalInt resolve(String token) {
        Integer userId = tokens.get(token);
        return userId == null ? OptionalInt.empty() : OptionalInt.of(userId);
    }
}
