package exceptions;
public class ServerRequestException extends Exception {
    public ServerRequestException(String message) {
        super(message);
    }

    public ServerRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}