package org.k1000o.ms.driver.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.cloud.discovery.enabled=false"
})
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${security.jwt.secret}")
    private String secret;

    @Test
    void actuatorHealthIsPublicAndReportsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/search"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/search")
                        .header("Authorization", "Bearer esto-no-es-un-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredTokenIsUnauthorized() throws Exception {
        String token = token(Instant.now().minusSeconds(120),
                List.of("USER"), List.of("driver:read"));

        mockMvc.perform(get("/search")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCanReadDrivers() throws Exception {
        mockMvc.perform(get("/search")
                        .header("Authorization", "Bearer " + validToken(
                                List.of("USER"), List.of("driver:read"))))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanReadDrivers() throws Exception {
        mockMvc.perform(get("/search")
                        .header("Authorization", "Bearer " + validToken(
                                List.of("ADMIN", "USER"), List.of("driver:read", "driver:write"))))
                .andExpect(status().isOk());
    }

    @Test
    void roleWithoutPermissionCannotReadDrivers() throws Exception {
        mockMvc.perform(get("/search")
                        .header("Authorization", "Bearer " + validToken(
                                List.of("USER"), List.of())))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCannotCreateDriver() throws Exception {
        mockMvc.perform(post("/")
                        .header("Authorization", "Bearer " + validToken(
                                List.of("USER"), List.of("driver:read")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Juan\",\"licenseNumber\":\"ABC-123\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateDriver() throws Exception {
        mockMvc.perform(post("/")
                        .header("Authorization", "Bearer " + validToken(
                                List.of("ADMIN", "USER"), List.of("driver:read", "driver:write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Juan\",\"licenseNumber\":\"ABC-123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    private String validToken(List<String> roles, List<String> permissions) throws Exception {
        return token(Instant.now().plusSeconds(3600), roles, permissions);
    }

    private String token(Instant expiration, List<String> roles, List<String> permissions) throws Exception {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("test-user")
                .issuer("ms-auth")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expiration))
                .claim("roles", roles)
                .claim("permissions", permissions)
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));
        return jwt.serialize();
    }
}
