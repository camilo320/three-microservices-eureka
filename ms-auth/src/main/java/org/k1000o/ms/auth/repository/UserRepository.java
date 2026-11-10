package org.k1000o.ms.auth.repository;

import org.k1000o.ms.auth.entity.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<AppUser, UUID> {

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<AppUser> findByUsername(String username);

    @Override
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    List<AppUser> findAll();

    boolean existsByUsername(String username);
}
