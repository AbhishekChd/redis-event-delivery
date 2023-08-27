package io.github.abhishekchd.rediseventdelivery.publisher;

import io.github.abhishekchd.rediseventdelivery.data.EventRepository;
import io.github.abhishekchd.rediseventdelivery.model.Event;
import io.github.abhishekchd.rediseventdelivery.model.EventEntity;
import io.github.abhishekchd.rediseventdelivery.model.EventStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
@Log4j2
public class RedisEventPublisher implements EventPublisher {
    @Autowired
    private RedisTemplate<String, Event> redisTemplate;

    @Autowired
    private ChannelTopic topic;

    @Autowired
    private EventRepository eventRepository;

    @Override
    public void publishMessage(Event event) {
        redisTemplate.convertAndSend(topic.getTopic(), event);
        persistEvent(event);
        log.info("Channel: {}, Message: {}", topic.getTopic(), event);
    }

    private void persistEvent(Event event) {
        EventEntity eventEntity = EventEntity.builder()
                .data(event.getPayload())
                .userId(event.getUserId())
                .eventCreatedAt(new Timestamp(System.currentTimeMillis()))
                .status(EventStatus.PENDING)
                .build();
        eventEntity = eventRepository.save(eventEntity);
        log.info("Saved entity to database. UUID: {}, Entity: {}", eventEntity.getSequenceId(), eventEntity);
    }
}
