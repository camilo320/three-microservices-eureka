package org.k1000o.ms.orden.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.k1000o.ms.orden.dto.OrdenRequest;
import org.k1000o.ms.orden.dto.OrdenResponse;
import org.k1000o.ms.orden.entity.Orden;
import org.k1000o.ms.orden.entity.Status;
import org.k1000o.ms.orden.exception.ResourceNotFoundException;
import org.k1000o.ms.orden.repository.OrdenRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
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
class OrdenServiceTest {

    @Mock
    private OrdenRepository repository;

    @InjectMocks
    private OrdenService service;

    @Test
    void savePersistsMappedOrderAndReturnsItsId() {
        UUID id = UUID.randomUUID();
        when(repository.save(any(Orden.class))).thenAnswer(invocation -> {
            Orden orden = invocation.getArgument(0);
            orden.setId(id);
            return orden;
        });

        UUID result = service.save(new OrdenRequest("Bogota", "Medellin"));

        assertThat(result).isEqualTo(id);

        ArgumentCaptor<Orden> captor = ArgumentCaptor.forClass(Orden.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getOrigin()).isEqualTo("Bogota");
        assertThat(captor.getValue().getDestination()).isEqualTo("Medellin");
    }

    @Test
    void getByIdReturnsMappedResponse() {
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 10, 8, 30);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 11, 9, 0);
        Orden orden = Orden.builder()
                .id(id)
                .origin("Bogota")
                .destination("Cali")
                .status(Status.IN_TRANSIT)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(orden));

        OrdenResponse response = service.getById(id);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.origin()).isEqualTo("Bogota");
        assertThat(response.destination()).isEqualTo("Cali");
        assertThat(response.status()).isEqualTo(Status.IN_TRANSIT);
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void getByIdThrowsWhenOrderDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void getOrdenByIdThrowsWhenOrderDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOrdenById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No hay orden con id : " + id);
    }

    @Test
    void updateModifiesOrderAndSavesIt() {
        UUID id = UUID.randomUUID();
        Orden orden = Orden.builder()
                .id(id)
                .origin("Bogota")
                .destination("Cali")
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(orden));

        service.update(id, new OrdenRequest("Cartagena", "Santa Marta"));

        assertThat(orden.getOrigin()).isEqualTo("Cartagena");
        assertThat(orden.getDestination()).isEqualTo("Santa Marta");
        verify(repository).save(orden);
    }

    @Test
    void updateThrowsWhenOrderDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, new OrdenRequest("Bogota", "Cali")))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).save(any(Orden.class));
    }

    @Test
    void searchReturnsMappedOrders() {
        Orden orden = Orden.builder()
                .id(UUID.randomUUID())
                .origin("Bogota")
                .destination("Cali")
                .status(Status.CREATED)
                .build();
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(orden));

        List<OrdenResponse> result = service.search(Status.CREATED, null, null, "bog", "cal");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).origin()).isEqualTo("Bogota");
        assertThat(result.get(0).destination()).isEqualTo("Cali");
        verify(repository).findAll(any(Specification.class));
    }

    @Test
    void searchAcceptsNullFiltersAndReturnsEmptyList() {
        when(repository.findAll(any(Specification.class))).thenReturn(List.of());

        List<OrdenResponse> result = service.search(null, null, null, null, null);

        assertThat(result).isEmpty();
        verify(repository).findAll(any(Specification.class));
    }

    @Test
    void toResponseReturnsNullWhenOrderIsNull() {
        assertThat(service.toResponse(null)).isNull();
    }
}
