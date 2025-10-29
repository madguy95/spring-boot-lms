package com.springjwt.core.actuator;

import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Actuator Configuration
 * Configure additional actuator features
 */
@Configuration
public class ActuatorConfig {

    /**
     * Enable HTTP trace (httpexchanges endpoint)
     * This tracks the last N HTTP requests/responses
     */
    @Bean
    public HttpExchangeRepository httpExchangeRepository() {
        InMemoryHttpExchangeRepository repository = new InMemoryHttpExchangeRepository();
        repository.setCapacity(100); // Store last 100 exchanges
        return repository;
    }
}
