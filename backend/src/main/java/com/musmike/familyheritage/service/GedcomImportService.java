package com.musmike.familyheritage.service;

import com.musmike.familyheritage.model.LifeState;
import com.musmike.familyheritage.model.Person;
import com.musmike.familyheritage.model.PersonGedcomData;
import com.musmike.familyheritage.model.Sex;
import com.musmike.familyheritage.repository.PersonRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.gedcom4j.model.Gedcom;
import org.gedcom4j.model.Individual;
import org.gedcom4j.model.PersonalName;
import org.gedcom4j.model.enumerations.IndividualEventType;
import org.gedcom4j.parser.GedcomParser;
import org.springframework.stereotype.Service;
import java.io.BufferedInputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class GedcomImportService {
    private final PersonRepository personRepository;
    private final PersonMatchingStrategy personMatchingStrategy;

    @Transactional
    public void importGedcom(InputStream inputstream) throws Exception {
        GedcomParser gp = new GedcomParser();
        gp.load(new BufferedInputStream(inputstream));
        Gedcom gedcom = gp.getGedcom();

        for (Individual indi : gedcom.getIndividuals().values()) {
            processIndividual(indi);
        }
    }

    private void processIndividual(Individual indi) {
        String getRefId = indi.getXref();

        Person person = personMatchingStrategy.findExistingPerson(indi)
            .orElseGet(() -> {
                Person newPerson = new Person();
                PersonGedcomData newGedcomData = new PersonGedcomData();
                newGedcomData.setGedRefId(getRefId);
                newPerson.setGedcomData(newGedcomData);
                newGedcomData.setPerson(newPerson);

                return newPerson;
            });

        String fullName = indi.getNames().isEmpty() ? "" : indi.getNames().getFirst().getBasic();
        NameParts parts = parseGedcomName(fullName);
        String rawSex = indi.getSex() != null ? indi.getSex().getValue() : null;
        boolean isDeceased = isDeceased(indi);

        person.setCachedGivenName(parts.givenName);
        person.setCachedSurname(parts.surname);
        person.setSex(mapGedcomSexToEnum(rawSex));
        person.setLifeState(isDeceased ? LifeState.DECEASED : LifeState.UNKNOWN);

        PersonGedcomData rawData = person.getGedcomData();
        rawData.setRawGivenName(parts.givenName());
        rawData.setRawSurname(parts.surname());
        rawData.setRawSex(rawSex != null && rawSex.length() > 1 ? rawSex.substring(0, 1) : rawSex);
        rawData.setRawDeathIndicated(isDeceased);

        personRepository.save(person);
    }

    private Sex mapGedcomSexToEnum(String sex) {
        if (sex == null) return Sex.UNKNOWN;
        return switch (sex.toUpperCase().trim()) {
            case "M" -> Sex.MALE;
            case "F" -> Sex.FEMALE;
            default -> Sex.UNKNOWN;
        };
    }

    private boolean isDeceased(Individual indi) {
        return !indi.getEventsOfType(IndividualEventType.DEATH).isEmpty();
    }

    private record NameParts(String givenName, String surname) {}

    private NameParts parseGedcomName(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return new NameParts("", "");
        }

        int start = fullName.indexOf('/');
        int end = fullName.lastIndexOf('/');

        if (start != -1 && end != -1 && start < end) {
            String surname = fullName.substring(start + 1, end).trim();
            String givenName = (fullName.substring(0, start) + fullName.substring(end + 1)).trim();
            return new NameParts(givenName, surname);
        } else {
            return new NameParts(fullName.trim(), "");
        }
    }
}
