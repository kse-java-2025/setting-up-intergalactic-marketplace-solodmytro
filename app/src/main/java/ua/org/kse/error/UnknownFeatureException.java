package ua.org.kse.error;

public class UnknownFeatureException extends RuntimeException {
    public UnknownFeatureException(String feature) {
        super(String.format("Unknown feature flag: %s", feature));
    }
}