package error;

public class InsufficientMemoryException extends Exception {
    public InsufficientMemoryException() {
        super("Insufficient memory for allocating given process.");
    }
}
