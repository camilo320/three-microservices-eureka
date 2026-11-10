package org.k1000o.ms.orden.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.k1000o.ms.orden.dto.OrdenResponse;
import org.k1000o.ms.orden.entity.Orden;
import org.k1000o.ms.orden.entity.Status;
import org.k1000o.ms.orden.repository.OrdenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(OrdenService.class)
class OrdenSearchIntegrationTest {

    @Autowired
    private OrdenService service;

    @Autowired
    private OrdenRepository repository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        persist("Bogota", "Cali", Status.CREATED, LocalDateTime.of(2026, 1, 10, 10, 0));
        persist("Medellin", "Bogota", Status.IN_TRANSIT, LocalDateTime.of(2026, 1, 12, 10, 0));
        persist("bogota", "Cartagena", Status.CREATED, LocalDateTime.of(2026, 1, 15, 10, 0));

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void searchWithoutFiltersReturnsAllOrders() {
        List<OrdenResponse> result = service.search(null, null, null, null, null);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(OrdenResponse::origin)
                .containsExactlyInAnyOrder("Bogota", "Medellin", "bogota");
    }

    @Test
    void searchByStatusReturnsMatchingOrders() {
        List<OrdenResponse> result = service.search(Status.CREATED, null, null, null, null);

        assertThat(result).hasSize(2)
                .allMatch(response -> response.status() == Status.CREATED);
    }

    @Test
    void searchByOriginIsPartialAndCaseInsensitive() {
        List<OrdenResponse> result = service.search(null, null, null, "BOG", null);

        assertThat(result).extracting(OrdenResponse::origin)
                .containsExactlyInAnyOrder("Bogota", "bogota");
    }

    @Test
    void searchByDestinationIsPartialAndCaseInsensitive() {
        List<OrdenResponse> result = service.search(null, null, null, null, "cali");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).destination()).isEqualTo("Cali");
    }

    @Test
    void searchByFromDateReturnsOrdersCreatedOnOrAfterDate() {
        List<OrdenResponse> result = service.search(
                null, LocalDateTime.of(2026, 1, 12, 0, 0), null, null, null);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(OrdenResponse::destination)
                .containsExactlyInAnyOrder("Bogota", "Cartagena");
    }

    @Test
    void searchByToDateReturnsOrdersCreatedBeforeDate() {
        List<OrdenResponse> result = service.search(
                null, null, LocalDateTime.of(2026, 1, 13, 0, 0), null, null);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(OrdenResponse::destination)
                .containsExactlyInAnyOrder("Cali", "Bogota");
    }

    @Test
    void searchByDateRangeReturnsOrdersInsideRange() {
        List<OrdenResponse> result = service.search(
                null,
                LocalDateTime.of(2026, 1, 11, 0, 0),
                LocalDateTime.of(2026, 1, 14, 0, 0),
                null,
                null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).origin()).isEqualTo("Medellin");
    }

    @Test
    void searchCombinesStatusOriginAndDateFilters() {
        List<OrdenResponse> result = service.search(
                Status.CREATED,
                LocalDateTime.of(2026, 1, 14, 0, 0),
                null,
                "bog",
                null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).destination()).isEqualTo("Cartagena");
    }

    @Test
    void searchIgnoresBlankOriginAndDestination() {
        List<OrdenResponse> result = service.search(null, null, null, "  ", "");

        assertThat(result).hasSize(3);
    }

    @Test
    void searchReturnsEmptyListWhenNothingMatches() {
        List<OrdenResponse> result = service.search(
                Status.CANCELLED, null, null, null, null);

        assertThat(result).isEmpty();
    }

    private void persist(String origin, String destination, Status status, LocalDateTime createdAt) {
        Orden orden = repository.save(Orden.builder()
                .origin(origin)
                .destination(destination)
                .status(status)
                .build());
        entityManager.flush();

        // JPA auditing overwrites @CreatedDate on insert, so set it explicitly for date filters.
        entityManager.createQuery("update Orden o set o.createdAt = :createdAt where o.id = :id")
                .setParameter("createdAt", createdAt)
                .setParameter("id", orden.getId())
                .executeUpdate();
    }
}
