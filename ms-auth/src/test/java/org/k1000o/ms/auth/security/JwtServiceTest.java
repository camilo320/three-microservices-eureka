package org.k1000o.ms.auth.security;

import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.k1000o.ms.auth.config.SecurityConfig;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET =
            "three-microservices-eureka-jwt-secret-key-2026-change-me-in-production";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        SecurityConfig config = new SecurityConfig();
        var key = config.jwtSecretKey(SECRET);
        jwtService = new JwtService(config.jwtEncoder(key), 3_600_000);
    }

    @Test
    void generatesSignedTokenWithSubjectRolesAndPermissions() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("orden:read"),
                        new SimpleGrantedAuthority("user:write")));

        String token = jwtService.generateToken(authentication);

        SignedJWT signedJWT = SignedJWT.parse(token);
        assertThat(signedJWT.verify(new MACVerifier(SECRET.getBytes(StandardCharsets.UTF_8)))).isTrue();

        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        assertThat(claims.getSubject()).isEqualTo("admin");
        assertThat(claims.getIssuer()).isEqualTo("ms-auth");
        assertThat(claims.getStringListClaim("roles")).containsExactlyInAnyOrder("ADMIN", "USER");
        assertThat(claims.getStringListClaim("permissions"))
                .containsExactlyInAnyOrder("orden:read", "user:write");
        assertThat(claims.getExpirationTime()).isAfter(claims.getIssueTime());
    }

    @Test
    void exposesExpirationInSeconds() {
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(3600);
    }
}
