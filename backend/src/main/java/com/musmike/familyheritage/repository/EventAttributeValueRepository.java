package com.musmike.familyheritage.repository;

import com.musmike.familyheritage.model.Event;
import com.musmike.familyheritage.model.EventAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventAttributeValueRepository extends JpaRepository<EventAttributeValue, Long> {
    void deleteByEvent(Event event);
}