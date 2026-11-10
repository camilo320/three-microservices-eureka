package org.k1000o.ms.auth.dto;

import java.util.List;

public record TokenResponse(
        String token,
        String type,
        long expiresIn,
        String username,
        List<String> roles,
        List<String> permissions
) {
}
