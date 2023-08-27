package io.github.abhishekchd.rediseventdelivery.model;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Timestamp;


// This will be extended to multi-table design. Every destination will have its own
// table of format events_destination_<destination_id>. That will ensure, both
// persistence and FIFO
@Table(name = "events")
@Entity
@Data
@Builder
public class EventEntity {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private String sequenceId;
    private String userId;
    private String data;
    private Timestamp eventCreatedAt;

    @Enumerated
    private EventStatus status;
}
