package ua.org.kse.error;

public class FeatureNotAvailableException extends RuntimeException {
    public FeatureNotAvailableException(String feature) {
        super(String.format("Feature %s is disabled", feature));
    }
}