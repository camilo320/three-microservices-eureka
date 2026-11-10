package org.k1000o.ms.auth.service;

import lombok.RequiredArgsConstructor;
import org.k1000o.ms.auth.dto.CreateUserRequest;
import org.k1000o.ms.auth.dto.RoleResponse;
import org.k1000o.ms.auth.dto.UserResponse;
import org.k1000o.ms.auth.entity.AppUser;
import org.k1000o.ms.auth.entity.Permission;
import org.k1000o.ms.auth.entity.Role;
import org.k1000o.ms.auth.exception.BadRequestException;
import org.k1000o.ms.auth.repository.RoleRepository;
import org.k1000o.ms.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> findUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> findRoles() {
        return roleRepository.findAll().stream()
                .map(role -> new RoleResponse(role.getName(), permissions(role)))
                .toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("El usuario ya existe: " + username);
        }

        Set<Role> roles = new HashSet<>();
        for (String roleName : request.roles()) {
            String normalized = roleName.trim().toUpperCase(Locale.ROOT);
            Role role = roleRepository.findByName(normalized)
                    .orElseThrow(() -> new BadRequestException("Rol no encontrado: " + roleName));
            roles.add(role);
        }

        AppUser user = new AppUser(username, passwordEncoder.encode(request.password()), true);
        user.getRoles().addAll(roles);
        return toResponse(userRepository.save(user));
    }

    private UserResponse toResponse(AppUser user) {
        Set<String> roles = new TreeSet<>();
        Set<String> permissions = new TreeSet<>();

        for (Role role : user.getRoles()) {
            roles.add(role.getName());
            permissions.addAll(permissions(role));
        }

        return new UserResponse(user.getUsername(), user.isEnabled(), List.copyOf(roles), List.copyOf(permissions));
    }

    private List<String> permissions(Role role) {
        return role.getPermissions().stream()
                .map(Permission::getName)
                .sorted()
                .toList();
    }
}
