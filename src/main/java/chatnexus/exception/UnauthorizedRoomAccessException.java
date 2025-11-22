package chatnexus.exception;

public class UnauthorizedRoomAccessException extends RuntimeException {
    public UnauthorizedRoomAccessException(String message) {
        super(message);
    }
}
