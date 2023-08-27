package io.github.abhishekchd.rediseventdelivery.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Log4j2
public class EventSubscriber implements MessageListener {
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {
        try {
            Event event = objectMapper.readValue(message.getBody(), Event.class);
            log.info("Received <{}>", event);
        } catch (IOException e) {
            log.error(e);
        } finally {
            log.info("Processed Message <{}>", message);
        }
    }
}
