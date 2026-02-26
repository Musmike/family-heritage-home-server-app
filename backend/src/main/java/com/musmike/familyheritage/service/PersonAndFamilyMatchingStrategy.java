package com.musmike.familyheritage.service;

import com.musmike.familyheritage.model.Family;
import com.musmike.familyheritage.model.Person;
import com.musmike.familyheritage.repository.FamilyRepository;
import com.musmike.familyheritage.repository.PersonRepository;
import com.musmike.familyheritage.util.GedcomUtils;
import lombok.RequiredArgsConstructor;
import org.gedcom4j.model.Individual;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersonAndFamilyMatchingStrategy {
    private final PersonRepository personRepository;
    private final FamilyRepository familyRepository;

    Optional<Person> findExistingPerson(Individual indi) {
        // MyHeritage _UID
        String uid = GedcomUtils.getCustomFact(indi, "_UID");
        if (uid != null) {
            Optional<Person> byUid = personRepository.findByGedcomDataGedUid(uid);
            if (byUid.isPresent()) return byUid;
        }

        // XREF (e.g. @I1@)
        return personRepository.findByGedcomDataGedRefId(indi.getXref());
    }

    Optional<Family> findExistingFamily(org.gedcom4j.model.Family fam, String uid) {
        // MyHeritage _UID
        if (uid != null) {
            Optional<Family> byUid = familyRepository.findByGedcomDataGedUid(uid);
            if (byUid.isPresent()) return byUid;
        }

        // XREF (e.g. @F1@)
        return familyRepository.findByGedcomDataGedRefId(fam.getXref());
    }
}
