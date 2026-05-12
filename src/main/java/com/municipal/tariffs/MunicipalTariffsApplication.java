package com.municipal.tariffs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.municipal")
@EntityScan(basePackages = "com.municipal.entity")
@EnableJpaRepositories(basePackages = "com.municipal.repository")
public class MunicipalTariffsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MunicipalTariffsApplication.class, args);
    }
}