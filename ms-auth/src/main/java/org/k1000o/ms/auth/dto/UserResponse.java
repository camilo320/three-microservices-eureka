package org.k1000o.ms.auth.dto;

import java.util.List;

public record UserResponse(
        String username,
        boolean enabled,
        List<String> roles,
        List<String> permissions
) {
}
