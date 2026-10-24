package at.technikum.application.routing;

import at.technikum.application.common.exception.BadRequestException;
import at.technikum.application.server.http.Request;
import at.technikum.application.server.http.Response;
import at.technikum.application.server.http.Status;
import at.technikum.application.server.util.Json;

import java.util.Map;

public abstract class Controller {
    protected Map<String, Object> body(Request request) {
        return Json.parseObject(request.body());
    }

    protected int pathInt(Map<String, String> pathParams, String name) {
        String raw = pathParams.get(name);
        if (raw == null) throw new BadRequestException(name.toUpperCase() + "_MISSING");
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            throw new BadRequestException(name.toUpperCase() + "_INVALID");
        }
    }

    protected Response ok(Object body) {
        return Response.json(Status.OK, body);
    }

    protected Response created(Object body) {
        return Response.json(Status.CREATED, body);
    }
}
