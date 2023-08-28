package io.github.abhishekchd.rediseventdelivery.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
@Configuration
@EnableAsync
@EnableAutoConfiguration
public class AppConfig {
    @Value("${event_scheduler.retry.count}")
    private Integer eventSchedulerRetryCount;

    @Value("${event_scheduler.wait_duration}")
    private Integer eventSchedulerWaitDuration;

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(5);
    }
}
