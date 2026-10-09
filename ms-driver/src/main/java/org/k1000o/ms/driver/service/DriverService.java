package org.k1000o.ms.driver.service;

import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.k1000o.ms.driver.dto.DriverRequest;
import org.k1000o.ms.driver.dto.DriverResponse;
import org.k1000o.ms.driver.entity.Driver;
import org.k1000o.ms.driver.exception.ResourceNotFoundException;
import org.k1000o.ms.driver.repository.DriverRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class DriverService {
    private final DriverRepository repository;

    public UUID save(DriverRequest req){
        return repository.save(fromRequest(req) ).getId();
    }
    public DriverResponse getById(UUID id){
        return this.toResponse( getDriverById(id) );
    }
    public List<DriverResponse> search(
            String name,
            String licenseNumber,
            Boolean active,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        Specification<Driver> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }
            if (name != null && !name.isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (licenseNumber != null && !licenseNumber.isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("licenseNumber")), "%" + licenseNumber.toLowerCase() + "%"));
            }
            if (fromDate != null) {
                Instant fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromInstant));
            }
            if (toDate != null) {
                Instant toInstant = toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                predicates.add(criteriaBuilder.lessThan(root.get("createdAt"), toInstant));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return repository.findAll(specification).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void update(UUID id, DriverRequest request) {
        Driver Driver = getDriverById(id);

        Driver.setName( request.name() );
        Driver.setLicenseNumber( request.licenseNumber() );

        repository.save(Driver);
    }
    private Driver getDriverById(UUID id){
        return repository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("No hay Driver con id : " + id));
    }
    private Driver fromRequest(DriverRequest req){
        return Driver.builder()
                .name(req.name())
                .licenseNumber(req.licenseNumber())
                .build();
    }
    private DriverResponse toResponse(Driver Driver){
        return DriverResponse.builder()
                .id(Driver.getId())
                .name(Driver.getName())
                .licenseNumber(Driver.getLicenseNumber())
                .active(Driver.getActive())
                .createdAt(Driver.getCreatedAt())
                .updatedAt(Driver.getUpdatedAt())
                .build();
    }
}
