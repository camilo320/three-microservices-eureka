package org.k1000o.ms.orden.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.k1000o.ms.orden.dto.OrdenRequest;
import org.k1000o.ms.orden.dto.OrdenResponse;
import org.k1000o.ms.orden.entity.Status;
import org.k1000o.ms.orden.service.OrdenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class OrdenController {
    private final OrdenService service;

    @GetMapping("/{id}")
    public ResponseEntity<OrdenResponse> getById(@PathVariable UUID id){
        return ResponseEntity.ok( service.getById(id) );
    }
    @GetMapping("/search")
    public ResponseEntity<List<OrdenResponse>> search(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam(required = false) LocalDateTime toDate,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination) {
        return ResponseEntity.ok(service.search(status, fromDate, toDate, origin, destination));
    }
    @PostMapping
    public ResponseEntity<UUID> save(@Valid @RequestBody OrdenRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(request));
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id,@Valid @RequestBody OrdenRequest request){
        service.update(id,request);
        return ResponseEntity.accepted().build();
    }
}
