package org.k1000o.ms.orden.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrdenTest {

    @Test
    void prePersistAssignsCreatedStatusWhenStatusIsNull() {
        Orden orden = Orden.builder()
                .origin("Bogota")
                .destination("Cali")
                .build();

        orden.prePersist();

        assertThat(orden.getStatus()).isEqualTo(Status.CREATED);
    }

    @Test
    void prePersistKeepsStatusWhenAlreadySet() {
        Orden orden = Orden.builder()
                .origin("Bogota")
                .destination("Cali")
                .status(Status.DELIVERED)
                .build();

        orden.prePersist();

        assertThat(orden.getStatus()).isEqualTo(Status.DELIVERED);
    }
}
