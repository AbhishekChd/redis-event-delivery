package io.github.abhishekchd.rediseventdelivery.scheduler;

import io.github.abhishekchd.rediseventdelivery.data.EventRepository;
import io.github.abhishekchd.rediseventdelivery.model.Event;
import io.github.abhishekchd.rediseventdelivery.model.EventEntity;
import io.github.abhishekchd.rediseventdelivery.publisher.EventPublisher;
import io.github.abhishekchd.rediseventdelivery.util.BeanProvider;
import org.springframework.lang.NonNull;

import java.util.concurrent.TimeUnit;


public class EventScheduler {
    private static volatile EventScheduler sInstance = null;

    private final EventPublisher eventPublisher;
    private final EventRepository eventRepository;
    private final EventStateLifecycle<EventEntity> lifecycle = new EventStateLifecycle<>() {
        @Override
        public void onSuccess(@NonNull EventEntity entity) {
            // Remove from database
            eventRepository.deleteById(entity.getSequenceId());
        }

        @Override
        public void onFailure(@NonNull EventEntity entity, @NonNull String message) {
            // TODO: Change try_count and check for retry_count and apply back_off
            entity.setTryCount(entity.getTryCount() + 1);
            eventRepository.save(entity);
        }
    };

    private EventScheduler() {
        this.eventPublisher = BeanProvider.getBean(EventPublisher.class);
        this.eventRepository = BeanProvider.getBean(EventRepository.class);
    }

    public static EventScheduler getInstance() {
        if (sInstance == null) {
            synchronized (EventScheduler.class) {
                if (sInstance == null) {
                    sInstance = new EventScheduler();
                }
            }
        }
        return sInstance;
    }

    public void scheduleEvent(EventEntity eventEntity) {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Here we check for success/failure
        eventPublisher.publishMessage(Event.from(eventEntity));

        // For now considering success
        lifecycle.onSuccess(eventEntity);
    }
}
