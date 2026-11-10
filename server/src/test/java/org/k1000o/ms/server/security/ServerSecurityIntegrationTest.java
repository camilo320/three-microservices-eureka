package org.k1000o.ms.server.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.register-with-eureka=false",
                "eureka.client.fetch-registry=false"
        })
class ServerSecurityIntegrationTest {

    private static final String USERNAME = "eureka";
    private static final String PASSWORD = "eureka123";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void actuatorHealthIsPublicAndReportsUp() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    void dashboardRequiresAuthentication() {
        ResponseEntity<String> response = restTemplate.getForEntity("/", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void dashboardIsAvailableWithValidCredentials() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(USERNAME, PASSWORD)
                .getForEntity("/", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Eureka");
    }

    @Test
    void eurekaApiRequiresAuthentication() {
        ResponseEntity<String> response = restTemplate.getForEntity("/eureka/apps", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void eurekaApiIsAvailableWithValidCredentials() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(USERNAME, PASSWORD)
                .getForEntity("/eureka/apps", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void invalidCredentialsAreRejected() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(USERNAME, "wrong-password")
                .getForEntity("/eureka/apps", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
