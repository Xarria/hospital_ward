package com.backend.hospitalward;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class HospitalWardApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalWardApplication.class, args);
    }

}
