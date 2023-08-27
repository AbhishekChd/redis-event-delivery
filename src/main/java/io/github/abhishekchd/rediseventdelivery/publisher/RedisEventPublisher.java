package io.github.abhishekchd.rediseventdelivery.publisher;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RedisEventPublisher implements EventPublisher {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ChannelTopic topic;

    @Override
    public void publishMessage(String data) {
        redisTemplate.convertAndSend(topic.getTopic(), data);
        log.info("Channel: {}, Messgae: {}", topic.getTopic(), data);
    }
}
