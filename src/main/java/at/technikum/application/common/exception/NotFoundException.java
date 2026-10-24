package at.technikum.application.common.exception;

import at.technikum.application.server.http.Status;

public class NotFoundException extends AppException {
    public NotFoundException(String message) {
        super(Status.NOT_FOUND, message);
    }
}
