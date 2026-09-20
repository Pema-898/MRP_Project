package at.technikum.application;

import at.technikum.mrp.server.SimpleHttpServer;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        SimpleHttpServer server = new SimpleHttpServer(8080);
        server.start();
    }
}
