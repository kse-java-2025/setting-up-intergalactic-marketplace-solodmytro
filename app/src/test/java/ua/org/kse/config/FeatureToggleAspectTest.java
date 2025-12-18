package ua.org.kse.config;

import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import ua.org.kse.error.FeatureNotAvailableException;
import ua.org.kse.error.UnknownFeatureException;

import java.util.List;
import ua.org.kse.service.CosmoCatService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class FeatureToggleAspectTest {
    @Test
    void guardedMethod_whenFeatureEnabled_allowsExecution() {
        FeatureToggleService featureService = mock(FeatureToggleService.class);
        when(featureService.isEnabled("cosmoCats")).thenReturn(true);

        FeatureToggleAspect aspect = new FeatureToggleAspect(featureService);
        CosmoCatService target = createProxy(new CosmoCatService(), aspect);

        List<String> cats = target.getCosmoCats();

        assertThat(cats).contains("Luna", "Orion", "Nebula");
        verify(featureService).isEnabled("cosmoCats");
        verifyNoMoreInteractions(featureService);
    }

    @Test
    void guardedMethod_whenFeatureDisabled_throwsFeatureNotAvailable() {
        FeatureToggleService featureService = mock(FeatureToggleService.class);
        when(featureService.isEnabled("cosmoCats")).thenReturn(false);

        FeatureToggleAspect aspect = new FeatureToggleAspect(featureService);
        CosmoCatService target = createProxy(new CosmoCatService(), aspect);

        assertThrows(FeatureNotAvailableException.class, target::getCosmoCats);

        verify(featureService).isEnabled("cosmoCats");
        verifyNoMoreInteractions(featureService);
    }

    @Test
    void guardedMethod_whenServiceThrowsUnknownFeature_propagates() {
        FeatureToggleService featureService = mock(FeatureToggleService.class);
        when(featureService.isEnabled(anyString()))
            .thenThrow(new UnknownFeatureException("cosmoCats"));

        FeatureToggleAspect aspect = new FeatureToggleAspect(featureService);
        CosmoCatService target = createProxy(new CosmoCatService(), aspect);

        assertThrows(UnknownFeatureException.class, target::getCosmoCats);
    }

    private static CosmoCatService createProxy(CosmoCatService target, FeatureToggleAspect aspect) {
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);
        return factory.getProxy();
    }
}