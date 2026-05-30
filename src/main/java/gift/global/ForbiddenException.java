package gift.global;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException() {
        super("Access denied.");
    }
}
