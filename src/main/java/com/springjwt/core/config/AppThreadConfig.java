package com.springjwt.core.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
@Slf4j
public class AppThreadConfig {

    /**
     * Configure Virtual Threads for @Async methods with MDC context propagation
     * Java 21+ feature enabled
     * Automatically propagates Correlation ID and other MDC values to async threads
     */
    @Bean(name = "applicationTaskExecutor")
    public AsyncTaskExecutor applicationTaskExecutor() {
        Executor virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

        // Wrap with MDC context propagation
        return new TaskExecutorAdapter(runnable -> {
            // Capture MDC context from parent thread
            Map<String, String> contextMap = MDC.getCopyOfContextMap();

            // Execute on virtual thread with MDC context
            virtualThreadExecutor.execute(() -> {
                try {
                    // Set MDC context in virtual thread
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    runnable.run();
                } finally {
                    // Clean up MDC after task completion
                    MDC.clear();
                }
            });
        });
    }

    /**
     * Configure Virtual Threads for Tomcat request handling
     * This makes every HTTP request run on a virtual thread
     * Java 21+ feature enabled
     */
    @Bean
    @ConditionalOnProperty(name = "app.virtual-threads.enabled", havingValue = "true", matchIfMissing = false)
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            log.info("Virtual threads enabled for Tomcat request handling");
        };
    }
}

