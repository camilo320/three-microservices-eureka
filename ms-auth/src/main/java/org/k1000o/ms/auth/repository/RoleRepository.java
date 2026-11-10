package org.k1000o.ms.auth.repository;

import org.k1000o.ms.auth.entity.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    @EntityGraph(attributePaths = "permissions")
    Optional<Role> findByName(String name);

    @Override
    @EntityGraph(attributePaths = "permissions")
    List<Role> findAll();
}
