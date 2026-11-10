package org.k1000o.ms.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.k1000o.ms.auth.dto.CreateUserRequest;
import org.k1000o.ms.auth.dto.RoleResponse;
import org.k1000o.ms.auth.dto.UserResponse;
import org.k1000o.ms.auth.service.UserAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> findUsers() {
        return ResponseEntity.ok(userAdminService.findUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userAdminService.createUser(request));
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponse>> findRoles() {
        return ResponseEntity.ok(userAdminService.findRoles());
    }
}
