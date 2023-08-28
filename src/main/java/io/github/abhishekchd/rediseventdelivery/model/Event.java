package io.github.abhishekchd.rediseventdelivery.model;

import lombok.Data;
import org.springframework.lang.NonNull;

@Data
public class Event {
    String userId;
    String payload;

    /**
     * Build instance of basic {@link Event} from persistent event entity {@link EventEntity}
     *
     * @param entity EventEntity to be used for creation of event
     * @return Instance of event
     */
    public static Event from(@NonNull EventEntity entity) {
        Event event = new Event();
        event.setUserId(entity.getUserId());
        event.setPayload(entity.getData());
        return event;
    }
}