package ua.org.kse.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class RestClientConfigTest {
    @Test
    void cosmicRestClient_createsClient() {
        RestClientConfig config = new RestClientConfig();
        CosmoExternalProperties props = new CosmoExternalProperties(
            "https://example.com",
            "/api/terms"
        );

        RestClient client = config.cosmicRestClient(props);

        assertThat(client).isNotNull();
    }
}