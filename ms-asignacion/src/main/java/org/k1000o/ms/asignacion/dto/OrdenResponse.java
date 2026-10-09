package org.k1000o.ms.asignacion.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record OrdenResponse(
        UUID id,
        String origin,
        String destination,
        Status status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
        ) {
}
