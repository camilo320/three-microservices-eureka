package org.k1000o.ms.asignacion.feign;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.k1000o.ms.asignacion.exception.ApiError;
import org.k1000o.ms.asignacion.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == HttpStatus.NOT_FOUND.value()) {

            String errorMessage = "Recurso no encontrado en el servicio externo.";

            if (response.body() != null) {
                try {
                    String jsonString = IOUtils.toString(response.body().asInputStream(), StandardCharsets.UTF_8);

                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    ApiError error = objectMapper.readValue(jsonString, ApiError.class);

                    errorMessage += error.message();
                } catch (IOException e) {
                    log.error(e.getMessage(),e);
                }
            }

            return new ResourceNotFoundException(errorMessage);
        }

        return defaultDecoder.decode(methodKey, response);
    }
}
