package org.k1000o.ms.asignacion;

import org.junit.jupiter.api.Test;
import org.k1000o.ms.asignacion.config.FileProperties;
import org.k1000o.ms.asignacion.controller.AsignacionController;
import org.k1000o.ms.asignacion.feign.DriverFeign;
import org.k1000o.ms.asignacion.feign.OrdenFeign;
import org.k1000o.ms.asignacion.repository.AsignacionRepository;
import org.k1000o.ms.asignacion.service.AsignacionService;
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
class AsignacionApplicationContextTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoadsWithExpectedBeans() {
        assertThat(context.getBean(AsignacionApplication.class)).isNotNull();
        assertThat(context.getBean(AsignacionController.class)).isNotNull();
        assertThat(context.getBean(AsignacionService.class)).isNotNull();
        assertThat(context.getBean(AsignacionRepository.class)).isNotNull();
        assertThat(context.getBean(FileProperties.class)).isNotNull();
        assertThat(context.getBean(OrdenFeign.class)).isNotNull();
        assertThat(context.getBean(DriverFeign.class)).isNotNull();
    }
}
