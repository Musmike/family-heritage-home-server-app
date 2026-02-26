package com.musmike.familyheritage.repository;

import com.musmike.familyheritage.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
