package ua.org.kse.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ua.org.kse.support.AbstractPostgresIT;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "feature.cosmoCats.enabled=false",
    "feature.kittyProducts.enabled=true"
})
class FeatureToggleServiceTest extends AbstractPostgresIT {
    @Autowired
    private FeatureToggleService featureToggleService;

    @Test
    void featureFlags_areReadFromProperties() {
        assertThat(featureToggleService.isCosmoCatsEnabled()).isFalse();
        assertThat(featureToggleService.isKittyProductsEnabled()).isTrue();
    }
}