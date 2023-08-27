package io.github.abhishekchd.rediseventdelivery.publisher;

public interface EventPublisher {

    /**
     * Publish event to Redis based messaging queue
     *
     * @param data Message/Event to be sent to queue
     */
    void publishMessage(String data);
}
