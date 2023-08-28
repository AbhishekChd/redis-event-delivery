package io.github.abhishekchd.rediseventdelivery.scheduler;

import io.github.abhishekchd.rediseventdelivery.config.AppConfig;
import io.github.abhishekchd.rediseventdelivery.data.EventRepository;
import io.github.abhishekchd.rediseventdelivery.model.Event;
import io.github.abhishekchd.rediseventdelivery.model.EventEntity;
import io.github.abhishekchd.rediseventdelivery.publisher.EventPublisher;
import io.github.abhishekchd.rediseventdelivery.util.BeanProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class EventScheduler {
    private static final Random RANDOM = new Random();
    private static volatile EventScheduler sInstance = null;
    private final int MAX_RETRY_COUNT;
    private final int WAIT_DURATION;
    private final EventPublisher eventPublisher;
    private final EventRepository eventRepository;
    private final ScheduledExecutorService scheduler;
    private final EventStateLifecycle<EventEntity> lifecycle = new EventStateLifecycle<>() {
        @Override
        public void onSuccess(@NonNull EventEntity entity) {
            // Remove from database
            eventRepository.deleteById(entity.getSequenceId());
            log.info("Event {} successfully published in {} retries", entity.getSequenceId(), entity.getTryCount());
        }

        @Override
        public void onFailure(@NonNull EventEntity entity, @NonNull String message) {
            entity.setTryCount(entity.getTryCount() + 1);

            // Remove event after "Max Retry Limit"
            if (entity.getTryCount() > MAX_RETRY_COUNT) {
                eventRepository.deleteById(entity.getSequenceId());
                log.info("Event {} failed - {}. Retry limit of {} reached.", entity.getSequenceId(), message, MAX_RETRY_COUNT);

            } else {
                eventRepository.save(entity);
                log.info("Event {} failed - {}. Pushing for retry.", entity.getSequenceId(), message);
            }
        }
    };

    private EventScheduler() {
        final int DEFAULT_MAX_RETRY_COUNT = 2;
        int DEFAULT_WAIT_DURATION = 500; // 500ms
        this.eventPublisher = BeanProvider.getBean(EventPublisher.class);
        this.eventRepository = BeanProvider.getBean(EventRepository.class);
        this.scheduler = BeanProvider.getBean(ScheduledExecutorService.class);

        AppConfig appConfig = BeanProvider.getBean(AppConfig.class);

        Integer retryCountValue = appConfig.getEventSchedulerRetryCount();
        MAX_RETRY_COUNT = Objects.isNull(retryCountValue) ? DEFAULT_MAX_RETRY_COUNT : retryCountValue;

        Integer waitDurationValue = appConfig.getEventSchedulerWaitDuration();
        WAIT_DURATION = Objects.isNull(waitDurationValue) ? DEFAULT_WAIT_DURATION : waitDurationValue;
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

    public void scheduleEvent() {
        EventEntity eventEntity = eventRepository.findFirst1ByOrderByEventCreatedAtAsc();
        log.info("Loaded event {} from database", eventEntity.getSequenceId());

        long delay = exponentialBackoffDelay(eventEntity.getTryCount());
        scheduler.schedule(() -> executePublishEvent(eventEntity), delay, TimeUnit.MILLISECONDS);
        log.info("Scheduled event {} with delay of {}", eventEntity.getSequenceId(), delay);
    }

    public void executePublishEvent(EventEntity eventEntity) {
        // Here we check for success/failure
        eventPublisher.publishMessage(Event.from(eventEntity));

        // For now considering success. We can verify retry/back-off by setting this to onFailure()
        lifecycle.onSuccess(eventEntity);
    }

    /**
     * Attempting an exponential backoff using a constant wait duration for each request.
     * WAIT_DURATION = w
     * Attempts      = n
     * backoff_delay = (w * 2^n) +- small_random_delay
     *
     * @param attempts Number of attempts already made to execute command
     * @return delay in milliseconds
     */
    private long exponentialBackoffDelay(int attempts) {
        return WAIT_DURATION * (1L << attempts) + RANDOM.nextLong(-9, 10);
    }
}
