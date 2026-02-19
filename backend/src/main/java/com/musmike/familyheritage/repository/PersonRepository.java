package com.musmike.familyheritage.repository;

import com.musmike.familyheritage.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findByGedcomDataGedRefId(String gedRefId);
}

