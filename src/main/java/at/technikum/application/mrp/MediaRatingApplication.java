
package at.technikum.application.mrp;

import at.technikum.application.common.database.ConnectionFactory;
import at.technikum.application.mrp.auth.AuthController;
import at.technikum.application.mrp.auth.AuthService;
import at.technikum.application.mrp.auth.PasswordHasher;
import at.technikum.application.mrp.auth.TokenService;
import at.technikum.application.mrp.media.MediaController;
import at.technikum.application.mrp.media.MediaRepository;
import at.technikum.application.mrp.media.MediaService;
import at.technikum.application.mrp.media.PostgresMediaRepository;
import at.technikum.application.mrp.user.PostgresUserRepository;
import at.technikum.application.mrp.user.UserRepository;
import at.technikum.application.routing.Application;
import at.technikum.application.routing.Router;
import at.technikum.application.server.http.Request;
import at.technikum.application.server.http.Response;

public class MediaRatingApplication implements Application {

    private final Router router = new Router();

    public MediaRatingApplication() {
        // Database connection
        ConnectionFactory connections = new ConnectionFactory();

        // Repositories
        UserRepository userRepository =
                new PostgresUserRepository(connections);

        MediaRepository mediaRepository =
                new PostgresMediaRepository(connections);

        // Authentication
        TokenService tokenService = new TokenService();
        PasswordHasher passwordHasher = new PasswordHasher();

        AuthService authService = new AuthService(
                userRepository,
                passwordHasher,
                tokenService
        );

        // Media management
        MediaService mediaService = new MediaService(mediaRepository);

        // Controllers
        AuthController authController =
                new AuthController(authService);

        MediaController mediaController =
                new MediaController(authService, mediaService);

        // Public endpoints
        router.post("/api/users/register", authController::register);
        router.post("/api/users/login", authController::login);

        // Protected media CRUD endpoints
        router.get("/api/media", mediaController::list);
        router.post("/api/media", mediaController::create);
        router.get("/api/media/{mediaId}", mediaController::get);
        router.put("/api/media/{mediaId}", mediaController::update);
        router.delete("/api/media/{mediaId}", mediaController::delete);
    }

    @Override
    public Response handle(Request request) {
        return router.route(request);
    }
}
