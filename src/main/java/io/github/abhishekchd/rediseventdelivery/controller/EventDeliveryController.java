package io.github.abhishekchd.rediseventdelivery.controller;

import io.github.abhishekchd.rediseventdelivery.model.Event;
import io.github.abhishekchd.rediseventdelivery.publisher.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class EventDeliveryController {

    @Autowired
    private EventPublisher eventPublisher;

    @PostMapping("/publish-event")
    public ResponseEntity<String> publishEventToRedis(@RequestBody Event event) {
        eventPublisher.publishMessage(event);
        return ResponseEntity.ok("Received event: " + event);
    }
}
