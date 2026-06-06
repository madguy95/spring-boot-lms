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

    @Value("${app.quartz.clustered:false}")
    private boolean clustered;

    @Value("${app.quartz.cluster-checkin-interval:20000}")
    private long clusterCheckinInterval;

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
        factory.setStartupDelay(10);
        factory.setOverwriteExistingJobs(false);

        log.info("Quartz Scheduler is ENABLED and will start automatically (clustered={})", clustered);

        return factory;
    }

    private Properties quartzProperties() {
        Properties properties = new Properties();

        properties.setProperty("org.quartz.scheduler.instanceName", "SpringScheduler");
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        properties.setProperty("org.quartz.threadPool.class", "com.springjwt.core.quartz.QuartzThreadPool");
        properties.setProperty("org.quartz.jobStore.class", "org.springframework.scheduling.quartz.LocalDataSourceJobStore");
        properties.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
        properties.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
        properties.setProperty("org.quartz.jobStore.isClustered", String.valueOf(clustered));
        properties.setProperty("org.quartz.jobStore.clusterCheckinInterval", String.valueOf(clusterCheckinInterval));
        properties.setProperty("org.quartz.jobStore.useProperties", "false");

        return properties;
    }
}
