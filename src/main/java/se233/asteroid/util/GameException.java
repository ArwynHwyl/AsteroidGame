package se233.asteroid.util;

public class GameException extends RuntimeException {
    public enum ErrorType {
        RESOURCE_LOADING_ERROR,
        SPRITE_PROCESSING_ERROR,
        GENERAL_ERROR
    }

    private final ErrorType errorType;

    public GameException(String message, ErrorType errorType, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public GameException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}