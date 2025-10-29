package com.springjwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class SpringBootSecurityJwtApplication {

    public static void main(String[] args) {
        // Global UTC setting
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(SpringBootSecurityJwtApplication.class, args);
    }

}
