package org.k1000o.ms.driver.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.k1000o.ms.driver.dto.DriverResponse;
import org.k1000o.ms.driver.entity.Driver;
import org.k1000o.ms.driver.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(DriverService.class)
class DriverSearchIntegrationTest {

    @Autowired
    private DriverService service;

    @Autowired
    private DriverRepository repository;

    private LocalDate dateOfJohn;
    private LocalDate dateOfJane;
    private LocalDate dateOfJohnny;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        Instant johnCreatedAt = Instant.parse("2026-01-10T12:00:00Z");
        Instant janeCreatedAt = Instant.parse("2026-01-12T12:00:00Z");
        Instant johnnyCreatedAt = Instant.parse("2026-01-15T12:00:00Z");

        persist("John Doe", "AB-123", true, johnCreatedAt);
        persist("Jane Smith", "CD-456", false, janeCreatedAt);
        persist("johnny", "EF-789", true, johnnyCreatedAt);

        dateOfJohn = toLocalDate(johnCreatedAt);
        dateOfJane = toLocalDate(janeCreatedAt);
        dateOfJohnny = toLocalDate(johnnyCreatedAt);
    }

    @Test
    void searchWithoutFiltersReturnsAllDrivers() {
        List<DriverResponse> result = service.search(null, null, null, null, null);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(DriverResponse::name)
                .containsExactlyInAnyOrder("John Doe", "Jane Smith", "johnny");
    }

    @Test
    void searchByActiveReturnsMatchingDrivers() {
        List<DriverResponse> result = service.search(null, null, true, null, null);

        assertThat(result).hasSize(2)
                .allMatch(DriverResponse::active);
        assertThat(result).extracting(DriverResponse::name)
                .containsExactlyInAnyOrder("John Doe", "johnny");
    }

    @Test
    void searchByNameIsPartialAndCaseInsensitive() {
        List<DriverResponse> result = service.search("JOHN", null, null, null, null);

        assertThat(result).extracting(DriverResponse::name)
                .containsExactlyInAnyOrder("John Doe", "johnny");
    }

    @Test
    void searchByLicenseNumberIsPartialAndCaseInsensitive() {
        List<DriverResponse> result = service.search(null, "cd-", null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Jane Smith");
    }

    @Test
    void searchByFromDateReturnsDriversCreatedOnOrAfterDate() {
        List<DriverResponse> result = service.search(null, null, null, dateOfJane, null);

        assertThat(result).extracting(DriverResponse::name)
                .containsExactlyInAnyOrder("Jane Smith", "johnny");
    }

    @Test
    void searchByToDateIncludesTheWholeDay() {
        List<DriverResponse> result = service.search(null, null, null, null, dateOfJane);

        assertThat(result).extracting(DriverResponse::name)
                .containsExactlyInAnyOrder("John Doe", "Jane Smith");
    }

    @Test
    void searchByDateRangeReturnsDriversInsideRange() {
        List<DriverResponse> result = service.search(null, null, null, dateOfJane, dateOfJane);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Jane Smith");
    }

    @Test
    void searchCombinesActiveAndNameFilters() {
        List<DriverResponse> result = service.search("john", null, true, null, null);

        assertThat(result).extracting(DriverResponse::name)
                .containsExactlyInAnyOrder("John Doe", "johnny");
    }

    @Test
    void searchIgnoresBlankNameAndLicenseNumber() {
        List<DriverResponse> result = service.search("  ", "", null, null, null);

        assertThat(result).hasSize(3);
    }

    @Test
    void searchReturnsEmptyListWhenNothingMatches() {
        List<DriverResponse> result = service.search(null, null, null, dateOfJohnny.plusDays(1), null);

        assertThat(result).isEmpty();
    }

    private void persist(String name, String licenseNumber, boolean active, Instant createdAt) {
        repository.save(Driver.builder()
                .name(name)
                .licenseNumber(licenseNumber)
                .active(active)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build());
    }

    private LocalDate toLocalDate(Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
