package io.github.abhishekchd.rediseventdelivery.publisher;

import io.github.abhishekchd.rediseventdelivery.model.Event;

public interface EventPublisher {

    /**
     * Publish event to Redis based messaging queue
     *
     * @param event Message/Event to be sent to queue
     */
    void publishMessage(Event event);
}
