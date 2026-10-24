package at.technikum.application.common.exception;

import at.technikum.application.server.http.Status;

public class MethodNotAllowedException extends AppException {
    public MethodNotAllowedException(String message) {
        super(Status.METHOD_NOT_ALLOWED, message);
    }
}
