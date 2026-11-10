package org.k1000o.ms.orden;

import org.junit.jupiter.api.Test;
import org.k1000o.ms.orden.controller.OrdenController;
import org.k1000o.ms.orden.repository.OrdenRepository;
import org.k1000o.ms.orden.service.OrdenService;
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
class OrdenApplicationContextTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoadsWithExpectedBeans() {
        assertThat(context.getBean(OrdenApplication.class)).isNotNull();
        assertThat(context.getBean(OrdenController.class)).isNotNull();
        assertThat(context.getBean(OrdenService.class)).isNotNull();
        assertThat(context.getBean(OrdenRepository.class)).isNotNull();
    }
}
