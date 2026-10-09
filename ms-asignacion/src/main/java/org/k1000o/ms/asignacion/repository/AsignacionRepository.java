package org.k1000o.ms.asignacion.repository;

import org.k1000o.ms.asignacion.entity.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AsignacionRepository extends JpaRepository<Asignacion, UUID>, JpaSpecificationExecutor<Asignacion> {
}
