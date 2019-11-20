package error;

public class UnsupportedIdException extends Exception {
    public UnsupportedIdException() {
        super("The id you picked cannot be used. Please try again.");
    }
}
