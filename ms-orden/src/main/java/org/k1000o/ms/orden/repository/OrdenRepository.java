package org.k1000o.ms.orden.repository;

import org.k1000o.ms.orden.entity.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface OrdenRepository extends JpaRepository<Orden, UUID>, JpaSpecificationExecutor<Orden> {
}
