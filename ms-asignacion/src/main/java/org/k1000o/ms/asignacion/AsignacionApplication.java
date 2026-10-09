package org.k1000o.ms.asignacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
public class AsignacionApplication {

    public static void main(String[] args) {
        SpringApplication.run(AsignacionApplication.class, args);
    }

}