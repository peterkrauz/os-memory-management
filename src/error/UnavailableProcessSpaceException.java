package error;

public class UnavailableProcessSpaceException extends Exception {
    public UnavailableProcessSpaceException() {
        super("No available space for a new process.");
    }
}
