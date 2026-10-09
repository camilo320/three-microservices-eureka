package org.k1000o.ms.driver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DriverRequest(
        @NotBlank(message = "name is requerido")
        @Size(min = 2, max = 255, message = "name debe ser de 2 a 255 caracteres")
        String name,
        @NotBlank(message = "License number es requerido")
        @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "License number solamente puede contener letras, digitos y guión")
        @Size(min = 3, max = 50, message = "License number debe ser de 3 a 50 caracteres")
        String licenseNumber
) {
}
