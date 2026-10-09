package org.k1000o.ms.asignacion.feign;

import org.k1000o.ms.asignacion.dto.DriverResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "ms-driver")
public interface DriverFeign {
    @GetMapping("/{id}")
    DriverResponse getById(@PathVariable UUID id);
}
