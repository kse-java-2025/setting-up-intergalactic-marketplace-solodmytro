package ua.org.kse.web;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.org.kse.dto.ProductListDto;
import ua.org.kse.service.ProductService;
import ua.org.kse.support.AbstractPostgresIT;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIssuerValidationIT extends AbstractPostgresIT {
    private static WireMockServer wireMock;
    private static RSAKey rsaJwkWithPrivate;
    private static String issuer;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProductService productService;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> issuer);
        r.add("cosmo.external.base-url", () -> issuer);
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        wireMock = new WireMockServer(options().dynamicPort());
        wireMock.start();

        issuer = wireMock.baseUrl();

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        rsaJwkWithPrivate = new RSAKey.Builder((RSAPublicKey) kp.getPublic())
            .privateKey((RSAPrivateKey) kp.getPrivate())
            .keyUse(KeyUse.SIGNATURE)
            .algorithm(JWSAlgorithm.RS256)
            .keyID("test-kid")
            .build();

        wireMock.stubFor(
            com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/.well-known/openid-configuration"))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                        {
                          "issuer": "%s",
                          "jwks_uri": "%s/.well-known/jwks.json"
                        }
                        """.formatted(issuer, issuer))));

        JWKSet jwkSet = new JWKSet(rsaJwkWithPrivate.toPublicJWK());

        wireMock.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/.well-known/jwks.json"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(JSONObjectUtils.toJSONString(jwkSet.toJSONObject()))));
    }

    @AfterAll
    static void afterAll() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }

    @BeforeEach
    void setUp() {
        ProductListDto list = new ProductListDto();
        list.setItems(List.of());
        list.setPage(0);
        list.setSize(10);
        list.setTotalItems(0);
        list.setTotalPages(1);

        when(productService.getAll(0, 10)).thenReturn(list);
    }

    @Test
    void getAll_withValidIssuer_returns200() throws Exception {
        String token = mintJwt(issuer);

        mockMvc.perform(get("/api/v1/products")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void getAll_withWrongIssuer_returns401() throws Exception {
        String token = mintJwt("http://evil-issuer");

        mockMvc.perform(get("/api/v1/products")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    private static String mintJwt(String iss) throws Exception {
        Instant now = Instant.now();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
            .issuer(iss)
            .subject("test-user")
            .audience("cosmo-cats-api")
            .issueTime(java.util.Date.from(now))
            .expirationTime(java.util.Date.from(now.plusSeconds(600)))
            .claim("scope", "products:read")
            .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(rsaJwkWithPrivate.getKeyID())
            .type(JOSEObjectType.JWT)
            .build();

        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(new RSASSASigner(rsaJwkWithPrivate.toPrivateKey()));

        return jwt.serialize();
    }
}