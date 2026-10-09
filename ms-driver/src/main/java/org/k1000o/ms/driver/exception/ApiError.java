package org.k1000o.ms.driver.exception;

import lombok.Builder;

import java.util.Map;

@Builder
public record ApiError(
        String path,
        String message,
        int statusCode,
        //Instant timestamp,
        Map<String, String> errors
) {
}