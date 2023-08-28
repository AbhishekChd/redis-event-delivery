package io.github.abhishekchd.rediseventdelivery.controller;

import io.github.abhishekchd.rediseventdelivery.data.EventRepository;
import io.github.abhishekchd.rediseventdelivery.model.Event;
import io.github.abhishekchd.rediseventdelivery.model.EventEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class EventDeliveryController {

    @Autowired
    private EventRepository eventRepository;

    @PostMapping("/publish-event")
    public ResponseEntity<String> publishEventToRedis(@RequestBody Event event) {
        persistEvent(event);
        return ResponseEntity.ok("Received event: " + event);
    }

    private void persistEvent(Event event) {
        EventEntity eventEntity = new EventEntity(event.getUserId(), event.getPayload());
        eventEntity = eventRepository.save(eventEntity);
        log.info("Saved entity to database. UUID: {}, Entity: {}", eventEntity.getSequenceId(), eventEntity);
    }
}
