package io.github.abhishekchd.rediseventdelivery;

import io.github.abhishekchd.rediseventdelivery.scheduler.EventScheduler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RedisEventDeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisEventDeliveryApplication.class, args);
        EventScheduler.initEventWatcher();
    }

}
