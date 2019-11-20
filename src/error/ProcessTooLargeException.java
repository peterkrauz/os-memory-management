package error;

public class ProcessTooLargeException extends Exception {
    public ProcessTooLargeException() {
        super("Process too large to allocate.");
    }
}
