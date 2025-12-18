package ua.org.kse.external;

public class TagServiceException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Failed to fetch allowed cosmic tags";

    public TagServiceException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause);
    }
}