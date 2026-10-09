package org.k1000o.ms.asignacion.feign;

import org.k1000o.ms.asignacion.dto.OrdenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "ms-orden")
public interface OrdenFeign {
    @GetMapping("/{id}")
    OrdenResponse getById(@PathVariable UUID id);
}
