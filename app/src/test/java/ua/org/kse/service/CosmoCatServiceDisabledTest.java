package ua.org.kse.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ua.org.kse.error.FeatureNotAvailableException;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
    "feature.cosmoCats.enabled=false"
})
class CosmoCatServiceDisabledTest {
    @Autowired
    private CosmoCatService cosmoCatService;

    @Test
    void getCosmoCats_whenFeatureDisabled_throwsException() {
        assertThrows(FeatureNotAvailableException.class, () -> cosmoCatService.getCosmoCats());
    }
}