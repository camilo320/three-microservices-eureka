package org.k1000o.ms.asignacion.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record AsignacionResponse(
        UUID id,
        OrdenResponse orden,
        DriverResponse driver,
        String pdfFileName,
        String imageFileName,
        LocalDateTime assignedAt,
        LocalDateTime updatedAt
) {
}
