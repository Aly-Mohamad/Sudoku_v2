package controller.exceptions;

public class InvalidGameException extends RuntimeException {
    public InvalidGameException(String message) {
        super(message);
    }

    public InvalidGameException(String message, Throwable cause) {
        super(message, cause);
    }
}
