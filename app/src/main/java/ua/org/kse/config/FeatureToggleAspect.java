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

    @Before("execution(* ua.org.kse.service.CosmoCatService.getCosmoCats(..))")
    public void checkCosmoCatsFeature() {
        if (!featureToggleService.isCosmoCatsEnabled()) {
            throw new FeatureNotAvailableException("Cosmo Cats feature is disabled");
        }
    }
}