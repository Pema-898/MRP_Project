package at.technikum.application;

import at.technikum.application.mrp.MediaRatingApplication;
import at.technikum.application.server.Server;

public class Main {
    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        MediaRatingApplication application = new MediaRatingApplication();
        Server server = new Server(port, application);
        server.start();
    }
}
