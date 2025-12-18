package ua.org.kse.config;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import ua.org.kse.error.FeatureNotAvailableException;

@Aspect
@Component
@RequiredArgsConstructor
public class FeatureToggleAspect {
    private final FeatureToggleService featureToggleService;

    @Before("@annotation(guard)")
    public void checkFeatureFlag(FeatureGuard guard) {
        String feature = guard.value();

        if (!featureToggleService.isEnabled(feature)) {
            throw new FeatureNotAvailableException(feature);
        }
    }
}