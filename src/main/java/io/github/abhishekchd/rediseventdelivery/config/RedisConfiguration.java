package io.github.abhishekchd.rediseventdelivery.config;

import io.github.abhishekchd.rediseventdelivery.model.Event;
import io.github.abhishekchd.rediseventdelivery.subscriber.EventSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.util.concurrent.Executors;

@Configuration
public class RedisConfiguration {
    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, topic());
        container.setTaskExecutor(Executors.newFixedThreadPool(4));
        return container;
    }

    @Bean
    RedisTemplate<String, Event> genericTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Event> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Event.class));
        return redisTemplate;
    }

    @Bean
    ChannelTopic topic() {
        return ChannelTopic.of("sample-queue");
    }

    @Bean
    MessageListenerAdapter listenerAdapter(EventSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber);
    }
}
