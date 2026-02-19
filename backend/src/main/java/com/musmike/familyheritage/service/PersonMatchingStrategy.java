package com.musmike.familyheritage.service;

import com.musmike.familyheritage.model.Person;
import com.musmike.familyheritage.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.gedcom4j.model.Individual;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersonMatchingStrategy {
    private final PersonRepository personRepository;

    Optional<Person> findExistingPerson(Individual indi) {
        var person = personRepository.findByGedcomDataGedRefId(indi.getXref());
        if (person.isPresent()) return person;

        return Optional.empty();
    }
}
