package org.k1000o.ms.asignacion.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.k1000o.ms.asignacion.dto.AsignacionRequest;
import org.k1000o.ms.asignacion.dto.AsignacionResponse;
import org.k1000o.ms.asignacion.service.AsignacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class AsignacionController  {
    private final AsignacionService service;
    @GetMapping("{id}")
    public ResponseEntity<AsignacionResponse> getById(@PathVariable UUID id){
        return ResponseEntity.ok(service.getById(id));
    }
    @PostMapping
    public ResponseEntity<UUID> asignar(@Valid @RequestBody AsignacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.asignar(request));
    }

    @PostMapping(value = "{id}/pdf", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadPdf(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        service.uploadPdf(id, file);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(value = "/{id}/image", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadImage(
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file) {
        service.uploadImage(id, file);
        return ResponseEntity.accepted().build();
    }
}
