package com.springjwt.core.quartz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.quartz.enabled", havingValue = "true", matchIfMissing = false)
public class QuartzConfig {

    private final ApplicationContext applicationContext;

    @Value("${app.quartz.auto-startup:true}")
    private boolean autoStartup;

    @Bean
    public SpringJobFactory jobFactory() {
        SpringJobFactory jobFactory = new SpringJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(
            DataSource dataSource,
            SpringJobFactory jobFactory) {

        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setJobFactory(jobFactory);
        factory.setQuartzProperties(quartzProperties());
        factory.setWaitForJobsToCompleteOnShutdown(true);
        factory.setAutoStartup(autoStartup);
        factory.setStartupDelay(10); // Wait 10 seconds before starting
        factory.setOverwriteExistingJobs(false); // Don't overwrite existing jobs

        if (autoStartup) {
            log.info("Quartz Scheduler is ENABLED and will start automatically");
        } else {
            log.warn("Quartz Scheduler is ENABLED but auto-startup is DISABLED - manual start required");
        }

        return factory;
    }

    private Properties quartzProperties() {
        Properties properties = new Properties();

        // Scheduler properties
        properties.setProperty("org.quartz.scheduler.instanceName", "SpringScheduler");
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");

        // Virtual Thread Pool
        properties.setProperty("org.quartz.threadPool.class", "com.springjwt.core.quartz.QuartzThreadPool");

        // JobStore properties
        properties.setProperty("org.quartz.jobStore.class", "org.springframework.scheduling.quartz.LocalDataSourceJobStore");
        properties.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        properties.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
        properties.setProperty("org.quartz.jobStore.isClustered", "true");
        properties.setProperty("org.quartz.jobStore.clusterCheckinInterval", "20000");
        properties.setProperty("org.quartz.jobStore.useProperties", "false");

        log.info("Quartz properties configured successfully");
        return properties;
    }
}
