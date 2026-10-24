package at.technikum.application.server;

import at.technikum.application.common.exception.AppException;
import at.technikum.application.routing.Application;
import at.technikum.application.server.http.Method;
import at.technikum.application.server.http.Request;
import at.technikum.application.server.http.Response;
import at.technikum.application.server.http.Status;
import at.technikum.application.server.util.QueryParams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Handler implements HttpHandler {
    private final Application application;

    public Handler(Application application) {
        this.application = application;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Response response;
        try {
            Request request = toRequest(exchange);
            response = application.handle(request);
        } catch (AppException ex) {
            response = error(ex.getStatus(), ex.getClass().getSimpleName(), ex.getMessage());
        } catch (IllegalArgumentException ex) {
            response = error(Status.BAD_REQUEST, "BadRequestException", "INVALID_REQUEST");
        } catch (Exception ex) {
            ex.printStackTrace();
            response = error(Status.INTERNAL_SERVER_ERROR, "InternalServerError", "INTERNAL_SERVER_ERROR");
        }
        send(exchange, response);
    }

    private Request toRequest(HttpExchange exchange) throws IOException {
        Method method = Method.from(exchange.getRequestMethod());
        String path = exchange.getRequestURI().getPath();
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return new Request(
                method,
                path,
                QueryParams.parse(exchange.getRequestURI().getRawQuery()),
                exchange.getRequestHeaders(),
                body
        );
    }

    private Response error(Status status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", error);
        body.put("message", message);
        return Response.json(status, body);
    }

    private void send(HttpExchange exchange, Response response) throws IOException {
        for (Map.Entry<String, String> header : response.headers().entrySet()) {
            exchange.getResponseHeaders().set(header.getKey(), header.getValue());
        }
        byte[] bytes = response.body().getBytes(StandardCharsets.UTF_8);
        if (response.status() == Status.NO_CONTENT) {
            exchange.sendResponseHeaders(response.status().code(), -1);
            exchange.close();
            return;
        }
        exchange.sendResponseHeaders(response.status().code(), bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
