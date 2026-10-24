package at.technikum.application.common.exception;

import at.technikum.application.server.http.Status;

public class BadRequestException extends AppException {
    public BadRequestException(String message) {
        super(Status.BAD_REQUEST, message);
    }
}
