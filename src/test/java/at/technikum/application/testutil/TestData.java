package at.technikum.application.testutil;

import at.technikum.application.mrp.media.Media;
import at.technikum.application.mrp.media.MediaType;
import at.technikum.application.mrp.user.User;

import java.util.List;

public final class TestData {
    private TestData() {}

    public static User user(int id, String name) {
        return new User(id, name, "hash", null, null);
    }

    public static Media media(int id, String title, MediaType type, List<String> genres, int age, int creatorId, double score) {
        return new Media(id, title, title + " description", type, 2020 + id % 5, genres, age,
                creatorId, "creator" + creatorId, score, 0);
    }
}
