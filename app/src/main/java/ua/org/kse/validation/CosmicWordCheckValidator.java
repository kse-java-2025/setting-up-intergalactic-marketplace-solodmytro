package ua.org.kse.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CosmicWordCheckValidator implements ConstraintValidator<CosmicWordCheck, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String v = value.toLowerCase();
        return v.contains("star") || v.contains("galaxy") || v.contains("comet");
    }
}