package ua.org.kse.error;

public class CosmicTagNotAllowedException extends RuntimeException {
    public CosmicTagNotAllowedException(String cosmicTag) {
        super(String.format("cosmicTag '%s' is not allowed by external dictionary", cosmicTag));
    }
}