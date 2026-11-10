package org.k1000o.ms.driver.exception;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void resourceNotFoundIsMappedToNotFound() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/drivers/1");

        ApiError error = handler
                .handleException(new ResourceNotFoundException("No hay Driver con id : 1"), request)
                .getBody();

        assertThat(error).isNotNull();
        assertThat(error.statusCode()).isEqualTo(404);
        assertThat(error.path()).isEqualTo("/api/drivers/1");
        assertThat(error.message()).isEqualTo("No hay Driver con id : 1");
        assertThat(error.errors()).isNull();
    }

    @Test
    void unexpectedExceptionIsMappedToInternalServerError() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/drivers");

        ApiError error = handler
                .handleException(new RuntimeException("boom"), request)
                .getBody();

        assertThat(error).isNotNull();
        assertThat(error.statusCode()).isEqualTo(500);
        assertThat(error.path()).isEqualTo("/api/drivers");
        assertThat(error.message()).isEqualTo("boomRuntimeException");
    }
}
