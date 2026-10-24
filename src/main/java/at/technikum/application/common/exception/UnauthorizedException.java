package at.technikum.application.common.exception;

import at.technikum.application.server.http.Status;

public class UnauthorizedException extends AppException {
    public UnauthorizedException(String message) {
        super(Status.UNAUTHORIZED, message);
    }
}
