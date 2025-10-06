package io.github.vivianagh.jobberwocky.exception;

public class ExternalSourceException extends RuntimeException {
    public ExternalSourceException(String message) {
        super(message);
    }

    public ExternalSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
