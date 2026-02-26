package com.musmike.familyheritage.repository;

import com.musmike.familyheritage.model.Grave;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GraveRepository extends JpaRepository<Grave, Long> {
}
