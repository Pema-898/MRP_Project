package at.technikum.application.common.exception;

import at.technikum.application.server.http.Status;

public abstract class AppException extends RuntimeException {
    private final Status status;

    protected AppException(Status status, String message) {
        super(message);
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
