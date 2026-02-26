package com.musmike.familyheritage.service;

import com.musmike.familyheritage.model.*;
import com.musmike.familyheritage.model.Family;
import com.musmike.familyheritage.model.FamilyChild;
import com.musmike.familyheritage.model.enums.*;
import com.musmike.familyheritage.repository.*;
import com.musmike.familyheritage.util.GedcomUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.gedcom4j.model.*;
import org.gedcom4j.model.enumerations.FamilyEventType;
import org.gedcom4j.model.enumerations.IndividualEventType;
import org.gedcom4j.parser.GedcomParser;
import org.springframework.stereotype.Service;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GedcomImportService {
    private final PersonRepository personRepository;
    private final PersonAndFamilyMatchingStrategy personAndFamilyMatchingStrategy;
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final UserCodeRepository userCodeRepository;
    private final EventTypeParticipantRoleRepository etprRepository;
    private final AttributeDefinitionRepository attrDefRepository;
    private final AttributeDictionaryValueRepository attrDictValueRepository;
    private final EventAttributeValueRepository eventAttrValueRepository;
    private final EventLocationRepository eventLocationRepository;
    private final GraveRepository graveRepository;
    private final BurialRepository burialRepository;
    private final EventParticipantRepository eventParticipantRepository;
    private final PersonNameRepository personNameRepository;
    private final FamilyRepository familyRepository;
    private final FamilyChildRepository familyChildRepository;

    private final GedcomDateParser dateParser;

    @Transactional
    public void importGedcom(InputStream inputstream) throws Exception {
        GedcomParser gp = new GedcomParser();
        gp.load(new BufferedInputStream(inputstream));
        Gedcom gedcom = gp.getGedcom();

        UserCode rolePrincipal = findSystemCode(CodeCategory.EVENT_PARTICIPANT_ROLE.name(),
                EventParticipantRole.PRINCIPAL.name());
        UserCode typeBirth = findSystemCode(CodeCategory.EVENT_TYPE.name(), EventType.BIRTH.name());
        UserCode typeDeath = findSystemCode(CodeCategory.EVENT_TYPE.name(), EventType.DEATH.name());
        UserCode typeBurial = findSystemCode(CodeCategory.EVENT_TYPE.name(), EventType.BURIAL.name());
        UserCode typeWedding = findSystemCode(CodeCategory.EVENT_TYPE.name(), EventType.WEDDING.name());

        UserCode statusMarried = findSystemCode(CodeCategory.FAMILY_STATUS.name(), FamilyStatus.MARRIED.name());
        UserCode statusUnknown = findSystemCode(CodeCategory.FAMILY_STATUS.name(), FamilyStatus.UNKNOWN.name());

        EventTypeParticipantRole birthRole = getEtprOrThrow(typeBirth, rolePrincipal);
        EventTypeParticipantRole deathRole = getEtprOrThrow(typeDeath, rolePrincipal);
        EventTypeParticipantRole burialRole = getEtprOrThrow(typeBurial, rolePrincipal);
        EventTypeParticipantRole weddingRole = getEtprOrThrow(typeWedding, rolePrincipal);

        AttributeDefinition causeOfDeathDef = attrDefRepository
                .findByUserCodeAndCode(typeDeath, DeathEventAttributeDefinition.CAUSE_OF_DEATH.name())
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Missing AttributeDefinition for DEATH -> %s",
                                DeathEventAttributeDefinition.CAUSE_OF_DEATH)));

        // Person Import
        for (Individual indi : gedcom.getIndividuals().values()) {
            processIndividual(indi, birthRole, deathRole, burialRole, causeOfDeathDef);
        }

        // Family Import
        for (org.gedcom4j.model.Family fam : gedcom.getFamilies().values()) {
            processFamily(fam, weddingRole, statusMarried, statusUnknown);
        }
    }

    private void processIndividual(Individual indi,
                                   EventTypeParticipantRole birthRole,
                                   EventTypeParticipantRole deathRole,
                                   EventTypeParticipantRole burialRole,
                                   AttributeDefinition causeOfDeathDef) {
        String gedRefId = indi.getXref();
        String uid = GedcomUtils.getCustomFact(indi, "_UID");

        Person person = personAndFamilyMatchingStrategy.findExistingPerson(indi)
            .orElseGet(() -> {
                Person p = new Person();
                PersonGedcomData d = new PersonGedcomData();
                d.setPerson(p);
                p.setGedcomData(d);
                return p;
            });

        // Basic Info
        String fullName = indi.getNames().isEmpty() ? "" : indi.getNames().getFirst().getBasic();
        NameParts parts = parseGedcomName(fullName);
        String rawSex = indi.getSex() != null ? indi.getSex().getValue() : null;
        boolean isDeceased = isDeceased(indi);

        person.setCachedGivenName(parts.givenName);
        person.setCachedSurname(parts.surname);
        person.setSex(mapGedcomSexToEnum(rawSex));
        person.setLifeState(isDeceased ? LifeState.DECEASED : LifeState.UNKNOWN);


        // Raw Info Basic
        PersonGedcomData rawData = person.getGedcomData();
        String previousBurialPlace = rawData.getRawBurialPlace();
        rawData.setGedRefId(gedRefId);
        rawData.setGedUid(uid);
        rawData.setRawGivenName(parts.givenName());
        rawData.setRawSurname(parts.surname());
        rawData.setRawSex(rawSex != null && rawSex.length() > 1 ? rawSex.substring(0, 1) : rawSex);
        rawData.setRawDeathIndicated(isDeceased);
        rawData.setLastImportedAt(LocalDateTime.now());

        person = personRepository.save(person);
        updatePersonName(person, parts);

        // SPECIFIC EVENTS PROCESSING

        // BIRTH
        List<IndividualEvent> births = indi.getEventsOfType(IndividualEventType.BIRTH);

        if (!births.isEmpty()) {
            IndividualEvent b = births.getFirst();
            rawData.setRawBirthDate(b.getDate() != null ? b.getDate().getValue() : null);
            rawData.setRawBirthPlace(b.getPlace() != null ? b.getPlace().getPlaceName() : null);

            Event savedBirth = processStandardEvent(b, person, birthRole,
                    person.getBirthEvent(), null);
            person.setBirthEvent(savedBirth); // Update cache in Person
        }

        // DEATH
        List<IndividualEvent> deaths = indi.getEventsOfType(IndividualEventType.DEATH);

        if (!deaths.isEmpty()) {
            IndividualEvent d = deaths.getFirst();
            rawData.setRawDeathDate(d.getDate() != null ? d.getDate().getValue() : null);
            rawData.setRawDeathPlace(d.getPlace() != null ? d.getPlace().getPlaceName() : null);
            rawData.setRawCauseOfDeath(d.getCause() != null ? d.getCause().getValue() : null);

            Event savedDeath = processStandardEvent(d, person, deathRole, person.getDeathEvent(), causeOfDeathDef);
            person.setDeathEvent(savedDeath); // Update cache in Person
        }

        // BURIAL
        List<IndividualEvent> burials = indi.getEventsOfType(IndividualEventType.BURIAL);
        if (!burials.isEmpty()) {
            IndividualEvent bur = burials.getFirst();
            rawData.setRawBurialPlace(bur.getPlace() != null ? bur.getPlace().getPlaceName() : null);

            Event existingBurialEvent = burialRepository.findByPerson(person).stream()
                    .filter(b -> b.getGrave() != null
                            && b.getGrave().getCemeteryLocation() != null
                            && b.getGrave().getCemeteryLocation().getName().equals(previousBurialPlace))
                    .map(Burial::getBurialEvent)
                    .findFirst()
                    .orElse(null);

            Event savedBurial = processStandardEvent(bur, person, burialRole, existingBurialEvent, null);
            processGraveAndBurial(savedBurial, person, previousBurialPlace);
        }

        personRepository.save(person);
    }

    private void processFamily(org.gedcom4j.model.Family gedFam,
                               EventTypeParticipantRole weddingRole,
                               UserCode statusMarried, UserCode statusUnknown) {
        String getRefId = gedFam.getXref();
        String uid = GedcomUtils.getCustomFact(gedFam, "_UID");

        Family family = personAndFamilyMatchingStrategy.findExistingFamily(gedFam, uid)
            .orElseGet(() -> {
                Family f = new Family();
                FamilyGedcomData d = new FamilyGedcomData();
                d.setFamily(f);
                f.setGedcomData(d);
                return f;
            });

        String husbRef = gedFam.getHusband() != null ? gedFam.getHusband().getIndividual().getXref() : null;
        String wifeRef = gedFam.getWife() != null ? gedFam.getWife().getIndividual().getXref(): null;

        if (husbRef != null) {
            personRepository.findByGedcomDataGedRefId(husbRef).ifPresent(family::setHusband);
        }
        if (wifeRef != null) {
            personRepository.findByGedcomDataGedRefId(wifeRef).ifPresent(family::setWife);
        }

        List<FamilyEvent> allEvents = gedFam.getEvents();

        List<FamilyEvent> marriages = allEvents.stream()
            .filter(e -> e.getType() == FamilyEventType.MARRIAGE)
            .collect(Collectors.toList());

        family.setStatus(marriages.isEmpty() ? statusUnknown : statusMarried);

        FamilyGedcomData raw = family.getGedcomData();
        raw.setGedRefId(getRefId);
        raw.setGedUid(uid);
        raw.setRawHusbandRefId(husbRef);
        raw.setRawWifeRefId(wifeRef);
        raw.setLastImportedAt(LocalDateTime.now());

        family = familyRepository.save(family);

        if (!marriages.isEmpty()) {
            org.gedcom4j.model.FamilyEvent marr = marriages.get(0);
            raw.setRawWeddingDate(marr.getDate() != null ? marr.getDate().getValue() : null);
            raw.setRawWeddingPlace(marr.getPlace() != null ? marr.getPlace().getPlaceName() : null);

            Event weddingEvent = processWeddingEvent(marr, family, weddingRole);
            family.setPrimaryWedding(weddingEvent);
        }

        if (gedFam.getChildren() != null) {
            for (IndividualReference childIndi : gedFam.getChildren()) {
                Family finalFamily = family;
                personRepository.findByGedcomDataGedRefId(childIndi.getIndividual().getXref()).ifPresent(childPerson -> {
                    boolean exists = finalFamily.getChildren().stream()
                            .anyMatch(fc -> fc.getChild().getId().equals(childPerson.getId()));

                    if (!exists) {
                        FamilyChild fc = new FamilyChild(finalFamily, childPerson, FamilyChildRelationshipType.BIOLOGICAL);
                        finalFamily.addChild(fc);
                    }
                });
            }
        }

        familyRepository.save(family);
    }

    private Event processStandardEvent(IndividualEvent gedEvent, Person person,
                   EventTypeParticipantRole role, Event existingEvent, AttributeDefinition attrDef) {
        Event event = existingEvent != null ? existingEvent : new Event();
        event.setEventType(role.getEventType());

        //  Date
        event.setStartDate(null);
        event.setEndDate(null);
        event.setCachedStartSortDateTime(null);
        event.setCachedEndSortDateTime(null);

        if (gedEvent.getDate() != null && gedEvent.getDate().getValue() != null) {
            var parsedDate = dateParser.parse(gedEvent.getDate().getValue());
            event.setStartDate(parsedDate.getStartDate());
            event.setEndDate(parsedDate.getEndDate());
            if (parsedDate.getStartDate() != null) {
                event.setCachedStartSortDateTime(calculateAverageDate(parsedDate.getStartDate()));
            }
            if (parsedDate.getEndDate() != null) {
                event.setCachedEndSortDateTime(calculateAverageDate(parsedDate.getEndDate()));
            }
        }

        // Location
        if (gedEvent.getPlace() != null && gedEvent.getPlace().getPlaceName() != null) {
            String placeName = gedEvent.getPlace().getPlaceName();
            Location location = locationRepository.findByName(placeName)
                    .orElseGet(() -> locationRepository.save(new Location(placeName)));
            event.setMainLocation(location);
            event = eventRepository.save(event);

            EventLocation el = (existingEvent == null)
                    ? new EventLocation()
                    : eventLocationRepository.findByEvent(event).orElseGet(EventLocation::new);

            el.setEvent(event);
            el.setLocation(location);
            eventLocationRepository.save(el);
        } else {
            event.setMainLocation(null);
            event = eventRepository.save(event);
        }

        // Participant (principal)
        if (existingEvent == null) {
            EventParticipant participant = new EventParticipant(event, person, role);
            eventParticipantRepository.save(participant);
        }

        // Event Attributes  (e.g. Cause of Death)
        if (attrDef != null) {
            String attrValue = gedEvent.getCause() != null ? gedEvent.getCause().getValue() : null;
            if (attrValue != null && !attrValue.isBlank()) {
                eventAttrValueRepository.deleteByEvent(event);
                saveEventAttribute(event, attrDef, attrValue);
            }
        }

        return event;
    }

    private Event processWeddingEvent(org.gedcom4j.model.FamilyEvent marr, Family family,
                                      EventTypeParticipantRole role) {
        Event event = family.getPrimaryWedding() != null ? family.getPrimaryWedding() : new Event();
        event.setEventType(role.getEventType());

        if (marr.getDate() != null && marr.getDate().getValue() != null) {
            var parsedDate = dateParser.parse(marr.getDate().getValue());
            event.setStartDate(parsedDate.getStartDate());
            event.setEndDate(parsedDate.getEndDate());
            if (parsedDate.getStartDate() != null) {
                event.setCachedStartSortDateTime(calculateAverageDate(parsedDate.getStartDate()));
            }
            if (parsedDate.getEndDate() != null) {
                event.setCachedEndSortDateTime(calculateAverageDate(parsedDate.getEndDate()));
            }
        }
        if (marr.getPlace() != null) {
            String placeName = marr.getPlace().getPlaceName();
            Location loc = locationRepository.findByName(placeName)
                    .orElseGet(() -> locationRepository.save(new Location(placeName)));
            event.setMainLocation(loc);
        }

        event = eventRepository.save(event);

        if (family.getHusband() != null) {
            addParticipantIfNotExists(event, family.getHusband(), role);
        }
        if (family.getWife() != null) {
            addParticipantIfNotExists(event, family.getWife(), role);
        }

        return eventRepository.save(event);
    }

    private void addParticipantIfNotExists(Event event, Person person, EventTypeParticipantRole role) {
        boolean exists = event.getParticipants().stream()
                .anyMatch(p -> p.getPerson().getId().equals(person.getId()));
        if (!exists) {
            EventParticipant part = new EventParticipant(event, person, role);
            eventParticipantRepository.save(part);
            event.addParticipant(part);
        }
    }

    private void updatePersonName(Person person, NameParts parts) {
        PersonName name = personNameRepository.findByPersonAndType(person, PersonNameType.BIRTH.name())
                .orElseGet(() -> new PersonName(person, PersonNameType.BIRTH.name(),
                        parts.givenName(), parts.surname(), true));

        name.setGivenName(parts.givenName());
        name.setSurname(parts.surname());
        name.setIsCurrent(true);

        personNameRepository.save(name);
    }

    private void processGraveAndBurial(Event burialEvent, Person person, String previousBurialPlace) {
        if (burialEvent.getMainLocation() != null) {
            List<Burial> existing = burialRepository.findByPerson(person);

            Burial burial = existing.stream()
                    .filter(b -> b.getGrave() != null
                            && b.getGrave().getCemeteryLocation() != null
                            && b.getGrave().getCemeteryLocation().getName().equals(previousBurialPlace))
                    .findFirst()
                    .orElseGet(Burial::new);

            Grave grave = burial.getGrave() != null ? burial.getGrave() : new Grave();
            grave.setCemeteryLocation(burialEvent.getMainLocation());
            grave = graveRepository.save(grave);

            burial.setGrave(grave);
            burial.setPerson(person);
            burial.setBurialEvent(burialEvent);
            burial.setIsCurrent(true);

            burialRepository.save(burial);
        }
    }

    private void saveEventAttribute(Event event, AttributeDefinition definition, String value) {
        AttributeDictionaryValue dictValue = attrDictValueRepository
                .findByAttributeDefinitionAndCode(definition, value)
                .orElseGet(() -> {
                    AttributeDictionaryValue newValue = new AttributeDictionaryValue();
                    newValue.setAttributeDefinition(definition);
                    newValue.setCode(value);
                    newValue.setIsSystem(false);
                    return attrDictValueRepository.save(newValue);
                });

        EventAttributeValue eventAttr = new EventAttributeValue();
        eventAttr.setEvent(event);
        eventAttr.setAttributeDictionaryValue(dictValue);
        eventAttrValueRepository.save(eventAttr);
    }

    private UserCode findSystemCode(String category, String code) {
        return userCodeRepository.findByCategoryAndCode(category, code)
                .orElseThrow(() -> new RuntimeException("System code " + category + "." + code + " not found"));
    }


    private EventTypeParticipantRole getEtprOrThrow(UserCode type, UserCode role) {
        return etprRepository.findByEventTypeAndParticipantRole(type, role)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Missing configuration for EventType: %s and Role: %s. Check migration scripts.",
                                type.getCode(), role.getCode())));
    }

    private LocalDateTime calculateAverageDate(DateDescriptor dd) {
        if (dd.getSortDateTimeMin() != null && dd.getSortDateTimeMax() != null) {
            long halfSeconds = Duration.between(dd.getSortDateTimeMin(), dd.getSortDateTimeMax()).getSeconds() / 2;
            return dd.getSortDateTimeMin().plusSeconds(halfSeconds);
        }
        return null;
    }

    private PersonSex mapGedcomSexToEnum(String sex) {
        if (sex == null) return PersonSex.UNKNOWN;
        return switch (sex.toUpperCase().trim()) {
            case "M" -> PersonSex.MALE;
            case "F" -> PersonSex.FEMALE;
            default -> PersonSex.UNKNOWN;
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
