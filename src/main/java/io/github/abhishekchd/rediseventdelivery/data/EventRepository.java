package io.github.abhishekchd.rediseventdelivery.data;

import io.github.abhishekchd.rediseventdelivery.model.EventEntity;
import org.springframework.data.repository.CrudRepository;

public interface EventRepository extends CrudRepository<EventEntity, String> {
}
