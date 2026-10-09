package org.k1000o.ms.orden.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrdenRequest(
        @NotBlank(message = "Origin is requerido")
        @Size(min = 2, max = 255, message = "Origin debe ser de 2 a 255 caracteres")
        String origin,
        @NotBlank(message = "destination is requerido")
        @Size(min = 2, max = 255, message = "destination debe ser de 2 a 255 caracteres")
        String destination
) {
}
