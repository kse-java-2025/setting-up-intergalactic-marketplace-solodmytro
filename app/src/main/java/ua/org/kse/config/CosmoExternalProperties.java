package ua.org.kse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cosmo.external")
public record CosmoExternalProperties(
    String baseUrl,
    String termsPath) {
}