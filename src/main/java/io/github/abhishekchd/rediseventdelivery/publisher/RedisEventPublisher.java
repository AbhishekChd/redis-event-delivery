package io.github.abhishekchd.rediseventdelivery.publisher;

import io.github.abhishekchd.rediseventdelivery.model.Event;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RedisEventPublisher implements EventPublisher {
    @Autowired
    private RedisTemplate<String, Event> redisTemplate;

    @Autowired
    private ChannelTopic topic;

    @Override
    public void publishMessage(Event event) {
        redisTemplate.convertAndSend(topic.getTopic(), event);
        log.info("Channel: {}, Message: {}", topic.getTopic(), event);
    }
}
