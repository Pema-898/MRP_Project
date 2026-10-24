package at.technikum.application.mrp.auth;

import at.technikum.application.common.exception.BadRequestException;
import at.technikum.application.common.exception.ConflictException;
import at.technikum.application.common.exception.UnauthorizedException;
import at.technikum.application.mrp.user.User;
import at.technikum.application.mrp.user.UserRepository;
import at.technikum.application.server.http.Request;

public class AuthService {
    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final TokenService tokens;

    public AuthService(UserRepository users, PasswordHasher passwordHasher, TokenService tokens) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokens = tokens;
    }

    public User register(Credentials credentials) {
        String username = credentials.username().trim();
        String password = credentials.password();
        if (username.length() < 3) throw new BadRequestException("USERNAME_TOO_SHORT");
        if (password.length() < 4) throw new BadRequestException("PASSWORD_TOO_SHORT");
        if (users.findByUsername(username).isPresent()) throw new ConflictException("USERNAME_ALREADY_EXISTS");
        return users.create(username, passwordHasher.hash(password));
    }

    public LoginResult login(Credentials credentials) {
        User user = users.findByUsername(credentials.username())
                .orElseThrow(() -> new UnauthorizedException("INVALID_CREDENTIALS"));
        if (!passwordHasher.verify(credentials.password(), user.passwordHash())) {
            throw new UnauthorizedException("INVALID_CREDENTIALS");
        }
        return new LoginResult(user, tokens.issue(user.username(), user.id()));
    }

    public User requireUser(Request request) {
        String header = request.header("Authorization")
                .orElseThrow(() -> new UnauthorizedException("AUTHORIZATION_REQUIRED"));
        if (!header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new UnauthorizedException("BEARER_TOKEN_REQUIRED");
        }
        String token = header.substring(7).trim();
        int userId = tokens.resolve(token)
                .orElseThrow(() -> new UnauthorizedException("INVALID_TOKEN"));
        return users.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("INVALID_TOKEN"));
    }

    public record LoginResult(User user, String token) {
    }
}
