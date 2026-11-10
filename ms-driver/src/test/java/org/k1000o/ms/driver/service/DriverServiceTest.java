package org.k1000o.ms.driver.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.k1000o.ms.driver.dto.DriverRequest;
import org.k1000o.ms.driver.dto.DriverResponse;
import org.k1000o.ms.driver.entity.Driver;
import org.k1000o.ms.driver.exception.ResourceNotFoundException;
import org.k1000o.ms.driver.repository.DriverRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository repository;

    @InjectMocks
    private DriverService service;

    @Test
    void savePersistsMappedDriverAndReturnsItsId() {
        UUID id = UUID.randomUUID();
        when(repository.save(any(Driver.class))).thenAnswer(invocation -> {
            Driver driver = invocation.getArgument(0);
            driver.setId(id);
            return driver;
        });

        UUID result = service.save(new DriverRequest("John Doe", "AB-123"));

        assertThat(result).isEqualTo(id);

        ArgumentCaptor<Driver> captor = ArgumentCaptor.forClass(Driver.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("John Doe");
        assertThat(captor.getValue().getLicenseNumber()).isEqualTo("AB-123");
    }

    @Test
    void getByIdReturnsMappedResponse() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-10T08:30:00Z");
        Instant updatedAt = Instant.parse("2026-01-11T09:00:00Z");
        Driver driver = Driver.builder()
                .id(id)
                .name("John Doe")
                .licenseNumber("AB-123")
                .active(true)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(driver));

        DriverResponse response = service.getById(id);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("John Doe");
        assertThat(response.licenseNumber()).isEqualTo("AB-123");
        assertThat(response.active()).isTrue();
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void getByIdThrowsWhenDriverDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No hay Driver con id : " + id);
    }

    @Test
    void updateModifiesDriverAndSavesIt() {
        UUID id = UUID.randomUUID();
        Driver driver = Driver.builder()
                .id(id)
                .name("John Doe")
                .licenseNumber("AB-123")
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(driver));

        service.update(id, new DriverRequest("Jane Doe", "CD-456"));

        assertThat(driver.getName()).isEqualTo("Jane Doe");
        assertThat(driver.getLicenseNumber()).isEqualTo("CD-456");
        verify(repository).save(driver);
    }

    @Test
    void updateThrowsWhenDriverDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, new DriverRequest("Jane Doe", "CD-456")))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).save(any(Driver.class));
    }

    @Test
    void searchReturnsMappedDrivers() {
        Driver driver = Driver.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .licenseNumber("AB-123")
                .active(true)
                .build();
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(driver));

        List<DriverResponse> result = service.search("John", "AB-123", true, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("John Doe");
        assertThat(result.get(0).active()).isTrue();
        verify(repository).findAll(any(Specification.class));
    }

    @Test
    void searchAcceptsNullFiltersAndReturnsEmptyList() {
        when(repository.findAll(any(Specification.class))).thenReturn(List.of());

        List<DriverResponse> result = service.search(null, null, null, null, null);

        assertThat(result).isEmpty();
        verify(repository).findAll(any(Specification.class));
    }
}
