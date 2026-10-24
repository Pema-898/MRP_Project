package at.technikum.application.routing;

import at.technikum.application.server.http.Method;
import at.technikum.application.server.http.Request;
import at.technikum.application.server.http.Response;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class Route {
    @FunctionalInterface
    public interface Endpoint {
        Response handle(Request request, Map<String, String> pathParams);
    }

    private final Method method;
    private final String[] templateParts;
    private final Endpoint endpoint;

    public Route(Method method, String template, Endpoint endpoint) {
        this.method = method;
        this.templateParts = split(normalize(template));
        this.endpoint = endpoint;
    }

    public Method method() {
        return method;
    }

    public Endpoint endpoint() {
        return endpoint;
    }

    public Optional<Map<String, String>> matchPath(String path) {
        String[] pathParts = split(normalize(path));
        if (pathParts.length != templateParts.length) return Optional.empty();

        Map<String, String> params = new LinkedHashMap<>();
        for (int i = 0; i < templateParts.length; i++) {
            String expected = templateParts[i];
            String actual = pathParts[i];
            if (expected.startsWith("{") && expected.endsWith("}")) {
                params.put(expected.substring(1, expected.length() - 1), actual);
            } else if (!expected.equals(actual)) {
                return Optional.empty();
            }
        }
        return Optional.of(params);
    }

    private static String normalize(String path) {
        if (path == null || path.isBlank()) return "/";
        String result = path.startsWith("/") ? path : "/" + path;
        while (result.length() > 1 && result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private static String[] split(String path) {
        if ("/".equals(path)) return new String[0];
        return path.substring(1).split("/");
    }
}
