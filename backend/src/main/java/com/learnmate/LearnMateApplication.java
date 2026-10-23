package com.learnmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LearnMateApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearnMateApplication.class, args);
    }
}
