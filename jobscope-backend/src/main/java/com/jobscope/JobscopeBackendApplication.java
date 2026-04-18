package com.jobscope;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JobscopeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobscopeBackendApplication.class, args);
    }

}
