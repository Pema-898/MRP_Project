package at.technikum.application.mrp.auth;

import at.technikum.application.common.exception.BadRequestException;
import at.technikum.application.common.exception.ConflictException;
import at.technikum.application.common.exception.UnauthorizedException;
import at.technikum.application.server.http.Method;
import at.technikum.application.server.http.Request;
import at.technikum.application.testutil.InMemoryUserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {
    @Test
    void registerCreatesUser() {
        var repo = new InMemoryUserRepository();
        var service = new AuthService(repo, new PasswordHasher(), new TokenService());
        var user = service.register(new Credentials("alice", "secret"));
        assertEquals("alice", user.username());
        assertNotEquals("secret", user.passwordHash());
    }

    @Test
    void duplicateUsernameIsRejected() {
        var repo = new InMemoryUserRepository();
        var service = new AuthService(repo, new PasswordHasher(), new TokenService());
        service.register(new Credentials("alice", "secret"));
        assertThrows(ConflictException.class, () -> service.register(new Credentials("Alice", "other")));
    }

    @Test
    void shortUsernameIsRejected() {
        var service = new AuthService(new InMemoryUserRepository(), new PasswordHasher(), new TokenService());
        assertThrows(BadRequestException.class, () -> service.register(new Credentials("ab", "secret")));
    }

    @Test
    void loginReturnsToken() {
        var repo = new InMemoryUserRepository();
        var tokens = new TokenService();
        var service = new AuthService(repo, new PasswordHasher(), tokens);
        service.register(new Credentials("alice", "secret"));
        var result = service.login(new Credentials("alice", "secret"));
        assertEquals("alice-mrpToken", result.token());
    }

    @Test
    void wrongPasswordIsRejected() {
        var repo = new InMemoryUserRepository();
        var service = new AuthService(repo, new PasswordHasher(), new TokenService());
        service.register(new Credentials("alice", "secret"));
        assertThrows(UnauthorizedException.class, () -> service.login(new Credentials("alice", "wrong")));
    }

    @Test
    void bearerTokenAuthenticatesUser() {
        var repo = new InMemoryUserRepository();
        var tokens = new TokenService();
        var service = new AuthService(repo, new PasswordHasher(), tokens);
        var user = service.register(new Credentials("alice", "secret"));
        String token = service.login(new Credentials("alice", "secret")).token();
        Request request = new Request(Method.GET, "/x", Map.of(), Map.of("Authorization", List.of("Bearer " + token)), "");
        assertEquals(user.id(), service.requireUser(request).id());
    }

    @Test
    void missingAuthorizationIsRejected() {
        var service = new AuthService(new InMemoryUserRepository(), new PasswordHasher(), new TokenService());
        Request request = new Request(Method.GET, "/x", Map.of(), Map.of(), "");
        assertThrows(UnauthorizedException.class, () -> service.requireUser(request));
    }
}
