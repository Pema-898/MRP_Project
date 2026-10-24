
package at.technikum.application.mrp.media;

import at.technikum.application.mrp.auth.AuthService;
import at.technikum.application.routing.Controller;
import at.technikum.application.server.http.Request;
import at.technikum.application.server.http.Response;

import java.util.Map;

public class MediaController extends Controller {

    private final AuthService auth;
    private final MediaService media;

    public MediaController(AuthService auth, MediaService media) {
        this.auth = auth;
        this.media = media;
    }

    // GET /api/media
    public Response list(Request request, Map<String, String> ignored) {
        auth.requireUser(request);

        return ok(media.search(
                MediaFilter.from(request.queryParams())
        ).stream().map(Media::toMap).toList());
    }

    // GET /api/media/{mediaId}
    public Response get(Request request, Map<String, String> path) {
        auth.requireUser(request);

        int mediaId = pathInt(path, "mediaId");

        return ok(media.get(mediaId).toMap());
    }

    // POST /api/media
    public Response create(Request request, Map<String, String> ignored) {
        var user = auth.requireUser(request);

        MediaInput input = MediaInput.from(body(request));

        return created(media.create(user, input).toMap());
    }

    // PUT /api/media/{mediaId}
    public Response update(Request request, Map<String, String> path) {
        var user = auth.requireUser(request);

        int mediaId = pathInt(path, "mediaId");
        MediaInput input = MediaInput.from(body(request));

        return ok(media.update(user, mediaId, input).toMap());
    }

    // DELETE /api/media/{mediaId}
    public Response delete(Request request, Map<String, String> path) {
        var user = auth.requireUser(request);

        int mediaId = pathInt(path, "mediaId");

        media.delete(user, mediaId);

        return Response.noContent();
    }
}
