package org.k1000o.ms.auth.config;

import lombok.RequiredArgsConstructor;
import org.k1000o.ms.auth.entity.AppUser;
import org.k1000o.ms.auth.entity.Permission;
import org.k1000o.ms.auth.entity.Role;
import org.k1000o.ms.auth.repository.PermissionRepository;
import org.k1000o.ms.auth.repository.RoleRepository;
import org.k1000o.ms.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Permission ordenRead = permission("orden:read");
        Permission ordenWrite = permission("orden:write");
        Permission driverRead = permission("driver:read");
        Permission driverWrite = permission("driver:write");
        Permission asignacionRead = permission("asignacion:read");
        Permission asignacionWrite = permission("asignacion:write");
        Permission userRead = permission("user:read");
        Permission userWrite = permission("user:write");

        Role userRole = role("USER", Set.of(
                ordenRead,
                driverRead,
                asignacionRead
        ));
        Role adminRole = role("ADMIN", Set.of(
                ordenRead, ordenWrite,
                driverRead, driverWrite,
                asignacionRead, asignacionWrite,
                userRead, userWrite
        ));

        user("user", "user123", Set.of(userRole));
        user("admin", "admin123", Set.of(adminRole, userRole));
    }

    private Permission permission(String name) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(new Permission(name)));
    }

    private Role role(String name, Set<Permission> permissions) {
        Role role = roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(name)));
        role.getPermissions().addAll(permissions);
        return roleRepository.save(role);
    }

    private void user(String username, String rawPassword, Set<Role> roles) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        AppUser user = new AppUser(username, passwordEncoder.encode(rawPassword), true);
        user.getRoles().addAll(roles);
        userRepository.save(user);
    }
}
