package org.k1000o.ms.auth.dto;

import java.util.List;

public record RoleResponse(
        String name,
        List<String> permissions
) {
}
