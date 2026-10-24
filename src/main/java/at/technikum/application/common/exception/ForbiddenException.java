package at.technikum.application.common.exception;

import at.technikum.application.server.http.Status;

public class ForbiddenException extends AppException {
    public ForbiddenException(String message) {
        super(Status.FORBIDDEN, message);
    }
}
