package ua.org.kse.external;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import ua.org.kse.config.CosmoExternalProperties;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CosmicDictionaryClientTest {
    private static WireMockServer wireMockServer;
    private CosmicDictionaryClient client;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();

        RestClient restClient = RestClient.builder()
            .baseUrl(wireMockServer.baseUrl())
            .build();

        client = new CosmicDictionaryClient(restClient, new CosmoExternalProperties(wireMockServer.baseUrl(), "/api/terms"));
    }

    @Test
    void fetchAllowedTerms_when200_returnsArray() {
        wireMockServer.stubFor(get(urlEqualTo("/api/terms"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[\"star\",\"galaxy\",\"comet\"]")));

        String[] terms = client.fetchAllowedTerms();

        assertThat(terms).containsExactly("star", "galaxy", "comet");
    }

    @Test
    void fetchAllowedTerms_whenServerErrors_throwsTagServiceException() {
        wireMockServer.stubFor(get(urlEqualTo("/api/terms"))
            .willReturn(aResponse()
                .withStatus(500)));

        assertThrows(TagServiceException.class, client::fetchAllowedTerms);
    }
}