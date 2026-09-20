package at.technikum.application.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Server {

    private final HttpServer server; //composition/"has-a" relationship

    public Server(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Very small first endpoints for class 3 progress.
        server.createContext("/api/health", this::handleHealth);
        server.createContext("/api/media", this::handleMedia);
    }

    public void start() {
        server.start();
        System.out.println("MRP server started on http://localhost:8080");
        System.out.println("Try: http://localhost:8080/api/health");
        System.out.println("Try: http://localhost:8080/api/media");
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        sendResponse(exchange, 200, "{\"status\":\"ok\"}");
    }

    private void handleMedia(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        // Temporary hard-coded response.
        // Later this will come from MediaService / MediaRepository / PostgreSQL.
        String response =
                "[\n" +
                "  {\n" +
                "    \"id\": 1,\n" +
                "    \"title\": \"Inception\",\n" +
                "    \"description\": \"Example media entry\",\n" +
                "    \"mediaType\": \"movie\",\n" +
                "    \"releaseYear\": 2010,\n" +
                "    \"genres\": [\"sci-fi\", \"thriller\"],\n" +
                "    \"ageRestriction\": 12\n" +
                "  }\n" +
                "]";

        sendResponse(exchange, 200, response);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBytes);
        }
    }
}
