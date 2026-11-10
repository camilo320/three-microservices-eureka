package org.k1000o.ms.asignacion.feign;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.k1000o.ms.asignacion.exception.ResourceNotFoundException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FeignErrorDecoderTest {

    private final FeignErrorDecoder decoder = new FeignErrorDecoder();

    @Test
    void notFoundWithApiErrorReturnsResourceNotFoundWithRemoteMessage() {
        String body = "{\"path\":\"/api/ordenes/1\",\"message\":\"No hay orden con id : 1\"," +
                "\"statusCode\":404,\"errors\":null}";

        Exception result = decoder.decode("OrdenFeign#getById(UUID)", response(404, body));

        assertThat(result)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Recurso no encontrado en el servicio externo.No hay orden con id : 1");
    }

    @Test
    void notFoundWithoutBodyReturnsResourceNotFoundWithGenericMessage() {
        Exception result = decoder.decode("OrdenFeign#getById(UUID)", response(404, null));

        assertThat(result)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Recurso no encontrado en el servicio externo.");
    }

    @Test
    void notFoundWithMalformedBodyReturnsResourceNotFoundWithGenericMessage() {
        Exception result = decoder.decode("OrdenFeign#getById(UUID)", response(404, "not-a-json"));

        assertThat(result)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Recurso no encontrado en el servicio externo.");
    }

    @Test
    void notFoundWithUnknownJsonPropertiesStillDeserializesMessage() {
        String body = "{\"message\":\"Orden inexistente\",\"statusCode\":404,\"unknown\":\"value\"}";

        Exception result = decoder.decode("OrdenFeign#getById(UUID)", response(404, body));

        assertThat(result)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Recurso no encontrado en el servicio externo.Orden inexistente");
    }

    @Test
    void otherStatusesAreDelegatedToDefaultDecoder() {
        Exception result = decoder.decode("OrdenFeign#getById(UUID)", response(500, "{}"));

        assertThat(result).isInstanceOf(FeignException.class);
    }

    private Response response(int status, String body) {
        Response.Builder builder = Response.builder()
                .status(status)
                .reason("reason")
                .request(request())
                .headers(Map.of("Content-Type", List.of("application/json")));
        return body == null ? builder.build() : builder.body(body, StandardCharsets.UTF_8).build();
    }

    private Request request() {
        return Request.create(
                Request.HttpMethod.GET,
                "http://localhost/api/ordenes/1",
                Map.of(),
                new byte[0],
                StandardCharsets.UTF_8);
    }
}
