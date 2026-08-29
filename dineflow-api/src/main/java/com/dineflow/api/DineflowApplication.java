package com.dineflow.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.dineflow")
@EnableJpaRepositories(basePackages = "com.dineflow")
@EntityScan(basePackages = "com.dineflow.domain.entity") // Entity'larni shu yerdan qidiradi
public class DineflowApplication {
    public static void main(String[] args) {
        SpringApplication.run(DineflowApplication.class, args);
    }
}