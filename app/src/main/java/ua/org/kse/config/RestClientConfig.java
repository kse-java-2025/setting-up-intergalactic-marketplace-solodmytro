package ua.org.kse.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(CosmoExternalProperties.class)
public class RestClientConfig {
    @Bean
    public RestClient cosmicRestClient(CosmoExternalProperties props) {
        String baseUrl = props.baseUrl() == null || props.baseUrl().isBlank()
            ? "http://localhost:8089"
            : props.baseUrl();

        return RestClient.builder()
            .baseUrl(baseUrl)
            .build();
    }
}