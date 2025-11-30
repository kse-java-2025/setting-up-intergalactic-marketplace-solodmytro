package ua.org.kse.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "feature.cosmoCats.enabled=false",
    "feature.kittyProducts.enabled=true"
})
class FeatureToggleServiceTest {
    @Autowired
    private FeatureToggleService featureToggleService;

    @Test
    void featureFlags_areReadFromProperties() {
        assertThat(featureToggleService.isCosmoCatsEnabled()).isFalse();
        assertThat(featureToggleService.isKittyProductsEnabled()).isTrue();
    }
}