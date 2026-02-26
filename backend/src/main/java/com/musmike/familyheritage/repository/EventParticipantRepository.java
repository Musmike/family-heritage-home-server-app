package com.musmike.familyheritage.repository;

import com.musmike.familyheritage.model.EventParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventParticipantRepository extends JpaRepository<EventParticipant, Long> {
}
