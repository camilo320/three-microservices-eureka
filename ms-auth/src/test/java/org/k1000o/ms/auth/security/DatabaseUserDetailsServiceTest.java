package org.k1000o.ms.auth.security;

import org.junit.jupiter.api.Test;
import org.k1000o.ms.auth.entity.AppUser;
import org.k1000o.ms.auth.entity.Permission;
import org.k1000o.ms.auth.entity.Role;
import org.k1000o.ms.auth.repository.PermissionRepository;
import org.k1000o.ms.auth.repository.RoleRepository;
import org.k1000o.ms.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(DatabaseUserDetailsService.class)
class DatabaseUserDetailsServiceTest {

    @Autowired
    private DatabaseUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Test
    void loadsRolesAndPermissionsAsAuthorities() {
        Permission readOrders = permissionRepository.save(new Permission("orden:read"));
        Permission createOrders = permissionRepository.save(new Permission("orden:write"));

        Role userRole = new Role("USER");
        userRole.getPermissions().add(readOrders);
        userRole = roleRepository.save(userRole);

        AppUser user = new AppUser("db-user", "encoded-password", true);
        user.getRoles().add(userRole);
        userRepository.save(user);

        UserDetails details = userDetailsService.loadUserByUsername("db-user");

        assertThat(details.getUsername()).isEqualTo("db-user");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_USER", "orden:read")
                .doesNotContain("orden:write");
    }

    @Test
    void disabledUserIsReportedAsDisabled() {
        Role role = roleRepository.save(new Role("USER"));
        AppUser user = new AppUser("disabled-user", "encoded-password", false);
        user.getRoles().add(role);
        userRepository.save(user);

        UserDetails details = userDetailsService.loadUserByUsername("disabled-user");

        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    void unknownUserThrowsUsernameNotFoundException() {
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing-user"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing-user");
    }
}
