package org.k1000o.ms.auth;

import org.junit.jupiter.api.Test;
import org.k1000o.ms.auth.controller.AuthController;
import org.k1000o.ms.auth.controller.UserAdminController;
import org.k1000o.ms.auth.repository.PermissionRepository;
import org.k1000o.ms.auth.repository.RoleRepository;
import org.k1000o.ms.auth.repository.UserRepository;
import org.k1000o.ms.auth.security.DatabaseUserDetailsService;
import org.k1000o.ms.auth.security.JwtService;
import org.k1000o.ms.auth.service.UserAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.cloud.discovery.enabled=false"
})
class AuthApplicationContextTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoadsWithExpectedBeans() {
        assertThat(context.getBean(AuthApplication.class)).isNotNull();
        assertThat(context.getBean(AuthController.class)).isNotNull();
        assertThat(context.getBean(UserAdminController.class)).isNotNull();
        assertThat(context.getBean(JwtService.class)).isNotNull();
        assertThat(context.getBean(DatabaseUserDetailsService.class)).isNotNull();
        assertThat(context.getBean(UserAdminService.class)).isNotNull();
        assertThat(context.getBean(UserRepository.class)).isNotNull();
        assertThat(context.getBean(RoleRepository.class)).isNotNull();
        assertThat(context.getBean(PermissionRepository.class)).isNotNull();
    }
}
