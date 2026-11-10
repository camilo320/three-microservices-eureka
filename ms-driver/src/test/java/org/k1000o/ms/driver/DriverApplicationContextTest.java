package org.k1000o.ms.driver;

import org.junit.jupiter.api.Test;
import org.k1000o.ms.driver.controller.DriverController;
import org.k1000o.ms.driver.repository.DriverRepository;
import org.k1000o.ms.driver.service.DriverService;
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
class DriverApplicationContextTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoadsWithExpectedBeans() {
        assertThat(context.getBean(DriverApplication.class)).isNotNull();
        assertThat(context.getBean(DriverController.class)).isNotNull();
        assertThat(context.getBean(DriverService.class)).isNotNull();
        assertThat(context.getBean(DriverRepository.class)).isNotNull();
    }
}
