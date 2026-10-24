package at.technikum.application.server;

import at.technikum.application.routing.Application;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public final class Server {
    private final int port;
    private final Application application;

    public Server(int port, Application application) {
        this.port = port;
        this.application = application;
    }

    public void start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new Handler(application));
            server.setExecutor(null);
            server.start();
            System.out.printf("MRP server listening on http://localhost:%d/api%n", port);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not start HTTP server", ex);
        }
    }
}
