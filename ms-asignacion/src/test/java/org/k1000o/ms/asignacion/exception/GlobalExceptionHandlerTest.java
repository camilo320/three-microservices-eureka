package org.k1000o.ms.asignacion.exception;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void resourceNotFoundIsMappedToNotFound() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/asignaciones/1");

        ApiError error = handler
                .handleException(new ResourceNotFoundException("Asignacion no encontrado con id : 1"), request)
                .getBody();

        assertThat(error).isNotNull();
        assertThat(error.statusCode()).isEqualTo(404);
        assertThat(error.path()).isEqualTo("/api/asignaciones/1");
        assertThat(error.message()).isEqualTo("Asignacion no encontrado con id : 1");
        assertThat(error.errors()).isNull();
    }

    @Test
    void badRequestIsMappedToBadRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/asignaciones");

        ApiError error = handler
                .handleBadRequestException(
                        new BadRequestException("Solamente archivos PDF estan permitidos"), request)
                .getBody();

        assertThat(error).isNotNull();
        assertThat(error.statusCode()).isEqualTo(400);
        assertThat(error.path()).isEqualTo("/api/asignaciones");
        assertThat(error.message()).isEqualTo("Solamente archivos PDF estan permitidos");
        assertThat(error.errors()).isNull();
    }

    @Test
    void unexpectedExceptionIsMappedToInternalServerError() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/asignaciones");

        ApiError error = handler
                .handleException(new RuntimeException("boom"), request)
                .getBody();

        assertThat(error).isNotNull();
        assertThat(error.statusCode()).isEqualTo(500);
        assertThat(error.path()).isEqualTo("/api/asignaciones");
        assertThat(error.message()).isEqualTo("boomRuntimeException");
    }
}
