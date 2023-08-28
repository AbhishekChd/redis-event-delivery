package io.github.abhishekchd.rediseventdelivery.data;

import io.github.abhishekchd.rediseventdelivery.model.EventEntity;
import io.github.abhishekchd.rediseventdelivery.scheduler.EventScheduler;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import org.springframework.scheduling.annotation.Async;

public class EventEntityListener {

    @PostPersist
    @PostUpdate
    @Async
    public void databaseUpdateCallback(EventEntity eventEntity) {
        EventScheduler eventScheduler = EventScheduler.getInstance();
        eventScheduler.scheduleEvent(eventEntity);
    }
}
