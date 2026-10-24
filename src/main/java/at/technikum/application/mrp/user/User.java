package at.technikum.application.mrp.user;

public record User(
        int id,
        String username,
        String passwordHash,
        String email,
        String favoriteGenre
) {
}
