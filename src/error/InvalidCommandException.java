package error;

public class InvalidCommandException extends Exception {
    public InvalidCommandException() {
        super("Oops! You must've typed something wrong. Please retry.");
    }
}
