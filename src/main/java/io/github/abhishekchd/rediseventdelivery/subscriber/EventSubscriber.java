package io.github.abhishekchd.rediseventdelivery.subscriber;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class EventSubscriber implements MessageListener {
    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {
        log.info("Received <{}>", message);
    }
}
