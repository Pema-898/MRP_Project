package at.technikum.application.common.exception;

import at.technikum.application.server.http.Status;

public class ConflictException extends AppException {
    public ConflictException(String message) {
        super(Status.CONFLICT, message);
    }
}
