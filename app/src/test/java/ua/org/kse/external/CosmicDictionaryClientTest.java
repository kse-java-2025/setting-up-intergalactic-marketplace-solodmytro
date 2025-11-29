package ua.org.kse.external;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CosmicDictionaryClientTest {
    private WireMockServer wireMockServer;
    private CosmicDictionaryClient client;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();

        RestClient restClient = RestClient.builder()
            .baseUrl(wireMockServer.baseUrl())
            .build();

        client = new CosmicDictionaryClient(restClient, new CosmicDictionaryMapper());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void isAllowedTag_whenTagContainsAllowedTerm_returnsTrue() {
        wireMockServer.stubFor(get(urlEqualTo("/api/terms"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[\"star\",\"galaxy\",\"comet\"]")));

        boolean result = client.isAllowedTag("galaxy-delicacy");

        assertThat(result).isTrue();
    }

    @Test
    void isAllowedTag_whenTagDoesNotContainAllowedTerm_returnsFalse() {
        wireMockServer.stubFor(get(urlEqualTo("/api/terms"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[\"star\",\"galaxy\",\"comet\"]")));

        boolean result = client.isAllowedTag("boring-cheese");

        assertThat(result).isFalse();
    }

    @Test
    void isAllowedTag_whenServerErrors_throwsTagServiceException() {
        wireMockServer.stubFor(get(urlEqualTo("/api/terms"))
            .willReturn(aResponse()
                .withStatus(500)));

        assertThrows(TagServiceException.class, () -> client.isAllowedTag("star-delicacy"));
    }

    @Test
    void isAllowedTag_whenTagIsNullOrBlank_returnsTrueWithoutCallingDictionary() {
        boolean nullResult = client.isAllowedTag(null);
        boolean blankResult = client.isAllowedTag("   ");

        assertThat(nullResult).isTrue();
        assertThat(blankResult).isTrue();
    }
}