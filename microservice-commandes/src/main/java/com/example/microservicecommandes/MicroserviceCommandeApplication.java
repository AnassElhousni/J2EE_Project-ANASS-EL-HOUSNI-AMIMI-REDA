package com.example.microservicecommandes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties
public class MicroserviceCommandeApplication {

        public static void main(String[] args) {
            SpringApplication.run(MicroserviceCommandeApplication.class, args);;
    }

}
