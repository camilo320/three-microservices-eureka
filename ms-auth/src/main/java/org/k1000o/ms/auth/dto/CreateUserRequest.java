package org.k1000o.ms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateUserRequest(
        @NotBlank(message = "username es requerido")
        @Size(min = 3, max = 100, message = "username debe ser de 3 a 100 caracteres")
        String username,
        @NotBlank(message = "password es requerido")
        @Size(min = 6, max = 100, message = "password debe ser de 6 a 100 caracteres")
        String password,
        @NotEmpty(message = "roles es requerido")
        List<@NotBlank(message = "cada rol es requerido") String> roles
) {
}
