package ua.org.kse.error;

public class CosmicTagNotAllowedException extends RuntimeException {
    public CosmicTagNotAllowedException(String cosmicTag) {
        super("cosmicTag '" + cosmicTag + "' is not allowed by external dictionary");
    }
}