package at.technikum.application.mrp.auth;

import at.technikum.application.routing.Controller;
import at.technikum.application.server.http.Request;
import at.technikum.application.server.http.Response;

import java.util.LinkedHashMap;
import java.util.Map;

public class AuthController extends Controller {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    public Response register(Request request, Map<String, String> ignored) {
        var user = auth.register(Credentials.from(body(request)));
        return created(Map.of("id", user.id(), "username", user.username()));
    }

    public Response login(Request request, Map<String, String> ignored) {
        var result = auth.login(Credentials.from(body(request)));
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("token", result.token());
        response.put("userId", result.user().id());
        response.put("username", result.user().username());
        return ok(response);
    }
}
