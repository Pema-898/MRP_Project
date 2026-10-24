package at.technikum.application.routing;

import at.technikum.application.server.http.Request;
import at.technikum.application.server.http.Response;

public interface Application {
    Response handle(Request request);
}
