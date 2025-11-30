package ua.org.kse.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "feature.cosmoCats.enabled=true"
})
class CosmoCatServiceEnabledTest {
    @Autowired
    private CosmoCatService cosmoCatService;

    @Test
    void getCosmoCats_whenFeatureEnabled_returnsList() {
        List<String> cats = cosmoCatService.getCosmoCats();

        assertThat(cats)
            .isNotNull()
            .isNotEmpty();
    }
}