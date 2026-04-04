package com.municipal.tariffs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.municipal")
@EntityScan("com.municipal.entity")
@EnableJpaRepositories("com.municipal.repository")
public class MunicipalTariffsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MunicipalTariffsApplication.class, args);
    }
}