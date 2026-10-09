package org.k1000o.ms.asignacion.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AsignacionRequest(
        @NotNull(message = "Orden id es requerido")
        UUID ordenId,
        @NotNull(message = "Driver id es requerido")
        UUID driverId
) {
}
