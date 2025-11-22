package ua.org.kse.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class RestClientConfigTest {
    @Test
    void cosmicRestClient_createsClient() {
        RestClientConfig config = new RestClientConfig();

        RestClient client = config.cosmicRestClient("https://example.com");

        assertThat(client).isNotNull();
    }
}