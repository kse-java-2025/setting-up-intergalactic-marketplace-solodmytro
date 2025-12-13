package tools;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;

public class JwtMaterialGenerator {
    public static void main(String[] args) throws Exception {
        String issuer = "http://localhost:8089";
        String audience = "cosmo-cats-api";
        String kid = "cosmo-kid-1";

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        RSAKey rsaJwkPrivate = new RSAKey.Builder((RSAPublicKey) kp.getPublic())
            .privateKey((RSAPrivateKey) kp.getPrivate())
            .keyUse(KeyUse.SIGNATURE)
            .algorithm(JWSAlgorithm.RS256)
            .keyID(kid)
            .build();

        RSAKey rsaJwkPublic = rsaJwkPrivate.toPublicJWK();

        JWKSet jwkSet = new JWKSet(rsaJwkPublic);

        Instant now = Instant.now();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
            .issuer(issuer)
            .subject("github-user-123")
            .audience(audience)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(3600)))
            .claim("scope", "products:read products:write")
            .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(kid)
            .type(JOSEObjectType.JWT)
            .build();

        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(new RSASSASigner(rsaJwkPrivate));

        System.out.println("===== JWKS (public) =====");
        System.out.println(jwkSet.toJSONObject());

        System.out.println("\n===== ACCESS TOKEN =====");
        System.out.println(jwt.serialize());
    }
}