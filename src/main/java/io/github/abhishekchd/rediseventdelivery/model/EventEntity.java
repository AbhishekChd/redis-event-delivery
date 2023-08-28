package io.github.abhishekchd.rediseventdelivery.model;


import io.github.abhishekchd.rediseventdelivery.data.EventEntityListener;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Timestamp;


// This will be extended to multi-table design. Every destination will have its own
// table of format events_destination_<destination_id>. That will ensure, both
// persistence and FIFO
@Table(name = "events")
@Entity
@EntityListeners(EventEntityListener.class)
@Data
@NoArgsConstructor
public class EventEntity {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private String sequenceId;
    private String userId;
    private String data;
    private Timestamp eventCreatedAt;
    private int tryCount;

    @Enumerated
    private EventStatus status;

    /**
     * Instantiate a basic event, based on newly occurred event. Will set the current time from System and set the
     * {@link EventEntity#status} as {@link EventStatus#PENDING}
     *
     * @param userId User id received from Event API
     * @param data   Payload sent from Event API
     */
    public EventEntity(String userId, String data) {
        this.userId = userId;
        this.data = data;
        this.eventCreatedAt = new Timestamp(System.currentTimeMillis());
        this.status = EventStatus.PENDING;
    }
}
