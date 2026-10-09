package org.k1000o.ms.driver.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.k1000o.ms.driver.dto.DriverRequest;
import org.k1000o.ms.driver.dto.DriverResponse;
import org.k1000o.ms.driver.service.DriverService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class DriverController {
    private final DriverService service;

    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getById(@PathVariable UUID id){
        return ResponseEntity.ok( service.getById(id) );
    }
    @GetMapping("/search")
    public ResponseEntity<List<DriverResponse>> search(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String licenseNumber) {
        return ResponseEntity.ok(service.search(name, licenseNumber, active, fromDate, toDate));
    }
    @PostMapping
    public ResponseEntity<UUID> save(@Valid @RequestBody DriverRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(request));
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id,@Valid @RequestBody DriverRequest request){
        service.update(id,request);
        return ResponseEntity.accepted().build();
    }
}
