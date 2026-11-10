package org.k1000o.ms.orden.exception;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void resourceNotFoundIsMappedToNotFound() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ordenes/1");

        ApiError error = handler
                .handleException(new ResourceNotFoundException("No hay orden con id : 1"), request)
                .getBody();

        assertThat(error).isNotNull();
        assertThat(error.statusCode()).isEqualTo(404);
        assertThat(error.path()).isEqualTo("/api/ordenes/1");
        assertThat(error.message()).isEqualTo("No hay orden con id : 1");
        assertThat(error.errors()).isNull();
    }

    @Test
    void unexpectedExceptionIsMappedToInternalServerError() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ordenes");

        ApiError error = handler
                .handleException(new RuntimeException("boom"), request)
                .getBody();

        assertThat(error).isNotNull();
        assertThat(error.statusCode()).isEqualTo(500);
        assertThat(error.path()).isEqualTo("/api/ordenes");
        assertThat(error.message()).isEqualTo("boomRuntimeException");
    }
}
