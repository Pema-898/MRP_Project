package at.technikum.application.routing;

import at.technikum.application.common.exception.MethodNotAllowedException;
import at.technikum.application.common.exception.NotFoundException;
import at.technikum.application.server.http.Method;
import at.technikum.application.server.http.Request;
import at.technikum.application.server.http.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Router {
    private final List<Route> routes = new ArrayList<>();

    public void get(String path, Route.Endpoint endpoint) {
        add(Method.GET, path, endpoint);
    }

    public void post(String path, Route.Endpoint endpoint) {
        add(Method.POST, path, endpoint);
    }

    public void put(String path, Route.Endpoint endpoint) {
        add(Method.PUT, path, endpoint);
    }

    public void delete(String path, Route.Endpoint endpoint) {
        add(Method.DELETE, path, endpoint);
    }

    public void add(Method method, String path, Route.Endpoint endpoint) {
        routes.add(new Route(method, path, endpoint));
    }

    public Response route(Request request) {
        boolean pathMatched = false;
        for (Route route : routes) {
            var match = route.matchPath(request.path());
            if (match.isEmpty()) continue;
            pathMatched = true;
            if (route.method() == request.method()) {
                Map<String, String> params = match.get();
                return route.endpoint().handle(request, params);
            }
        }

        if (pathMatched) throw new MethodNotAllowedException("METHOD_NOT_ALLOWED");
        throw new NotFoundException("ROUTE_NOT_FOUND");
    }
}
