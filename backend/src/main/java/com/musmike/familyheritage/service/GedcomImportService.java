package com.musmike.familyheritage.service;

import com.musmike.familyheritage.model.*;
import com.musmike.familyheritage.model.Family;
import com.musmike.familyheritage.model.FamilyChild;
import com.musmike.familyheritage.model.enums.*;
import com.musmike.familyheritage.repository.*;
import com.musmike.familyheritage.util.GedcomUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import java.util.*;

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
    private final FamilyChildGedcomDataRepository familyChildGedcomDataRepository;

    private final GedcomDateParser dateParser;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void importGedcom(InputStream inputstream) throws Exception {
        LocalDateTime importStartTime = LocalDateTime.now();

        GedcomParser gp = new GedcomParser();
        gp.load(new BufferedInputStream(inputstream));
        Gedcom gedcom = gp.getGedcom();

        UserCode rolePrincipal = findSystemCode(CodeCategory.EVENT_PARTICIPANT_ROLE.name(), EventParticipantRole.PRINCIPAL.name());
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
                .orElseThrow(() -> new IllegalStateException("Missing AttributeDefinition for CAUSE_OF_DEATH"));

        // Import People
        for (Individual indi : gedcom.getIndividuals().values()) {
            if (isDummy(indi)) continue; // Pomijamy puste rekordy generowane przez parser
            processIndividual(indi, birthRole, deathRole, burialRole, causeOfDeathDef);
        }

        personRepository.flush();

        // Import Families
        for (org.gedcom4j.model.Family fam : gedcom.getFamilies().values()) {
            processFamily(fam, weddingRole, statusMarried, statusUnknown);
        }

        entityManager.flush();
        entityManager.clear(); // Reset kontekstu by uniknąć TransientObjectException

        performCleanup(importStartTime);

        entityManager.flush();
        cleanUpOrphans();
    }

    private boolean isDummy(Individual indi) {
        return (indi.getNames() == null || indi.getNames().isEmpty())
                && (indi.getEvents() == null || indi.getEvents().isEmpty())
                && indi.getSex() == null;
    }

    private void performCleanup(LocalDateTime cutoff) {
        List<Family> oldFamilies = familyRepository.findAll().stream()
                .filter(f -> f.getGedcomData().getLastImportedAt().isBefore(cutoff))
                .toList();
        familyRepository.deleteAll(oldFamilies);
        familyRepository.flush();

        List<Person> oldPeople = personRepository.findAll().stream()
                .filter(p -> p.getGedcomData().getLastImportedAt().isBefore(cutoff))
                .toList();

        if (oldPeople.isEmpty()) return;

        List<Family> remainingFamilies = familyRepository.findAll();
        for (Family f : remainingFamilies) {
            boolean changed = false;
            if (f.getHusband() != null && oldPeople.contains(f.getHusband())) {
                f.setHusband(null);
                changed = true;
            }
            if (f.getWife() != null && oldPeople.contains(f.getWife())) {
                f.setWife(null);
                changed = true;
            }

            Iterator<FamilyChild> childIterator = f.getChildren().iterator();
            while (childIterator.hasNext()) {
                FamilyChild fc = childIterator.next();
                if (oldPeople.contains(fc.getChild())) {
                    childIterator.remove();
                    familyChildRepository.delete(fc);
                    changed = true;
                }
            }
            if (changed) familyRepository.save(f);
        }
        familyRepository.flush();

        personRepository.deleteAll(oldPeople);
        personRepository.flush();
    }

    private void cleanUpOrphans() {
        eventRepository.deleteOrphanEvents();
        graveRepository.deleteOrphanGraves();
        locationRepository.deleteOrphanLocations();
        attrDictValueRepository.deleteOrphanValues();
    }

    private void processIndividual(Individual indi, EventTypeParticipantRole birthRole, EventTypeParticipantRole deathRole, EventTypeParticipantRole burialRole, AttributeDefinition causeOfDeathDef) {
        String gedRefId = indi.getXref();
        String uid = GedcomUtils.getCustomFact(indi, "_UID");
        Person person = personAndFamilyMatchingStrategy.findExistingPerson(indi).orElseGet(() -> {
            Person p = new Person();
            PersonGedcomData d = new PersonGedcomData();
            d.setPerson(p); p.setGedcomData(d); return p;
        });

        List<PersonalName> names = indi.getNames() != null ? indi.getNames() : Collections.emptyList();
        String fullName = names.isEmpty() ? "" : names.get(0).getBasic();
        NameParts parts = parseGedcomName(fullName);
        String rawSex = indi.getSex() != null ? indi.getSex().getValue() : null;
        boolean isDeceased = isDeceased(indi);

        person.setCachedGivenName(parts.givenName);
        person.setCachedSurname(parts.surname);
        person.setSex(mapGedcomSexToEnum(rawSex));
        person.setLifeState(isDeceased ? LifeState.DECEASED : LifeState.UNKNOWN);

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

        // BIRTH
        List<IndividualEvent> births = indi.getEventsOfType(IndividualEventType.BIRTH);
        if (!births.isEmpty()) {
            IndividualEvent b = births.get(0);
            rawData.setRawBirthDate(b.getDate() != null ? b.getDate().getValue() : null);
            rawData.setRawBirthPlace(b.getPlace() != null ? b.getPlace().getPlaceName() : null);
            Event savedBirth = processStandardEvent(b, person, birthRole, person.getBirthEvent(), null);
            person.setBirthEvent(savedBirth);
        } else if (person.getBirthEvent() != null) {
            Event old = person.getBirthEvent();
            person.setBirthEvent(null);
            rawData.setRawBirthDate(null);
            rawData.setRawBirthPlace(null);
            personRepository.save(person);
            eventRepository.delete(old);
        }

        // DEATH
        List<IndividualEvent> deaths = indi.getEventsOfType(IndividualEventType.DEATH);
        if (!deaths.isEmpty()) {
            IndividualEvent d = deaths.get(0);
            rawData.setRawDeathDate(d.getDate() != null ? d.getDate().getValue() : null);
            rawData.setRawDeathPlace(d.getPlace() != null ? d.getPlace().getPlaceName() : null);
            rawData.setRawCauseOfDeath(d.getCause() != null ? d.getCause().getValue() : null);
            Event savedDeath = processStandardEvent(d, person, deathRole, person.getDeathEvent(), causeOfDeathDef);
            person.setDeathEvent(savedDeath);
        } else if (person.getDeathEvent() != null) {
            Event old = person.getDeathEvent();
            person.setDeathEvent(null);
            rawData.setRawDeathDate(null);
            rawData.setRawDeathPlace(null);
            rawData.setRawCauseOfDeath(null);
            personRepository.save(person);
            eventRepository.delete(old);
        }

        // BURIAL
        List<IndividualEvent> burials = indi.getEventsOfType(IndividualEventType.BURIAL);
        if (!burials.isEmpty()) {
            IndividualEvent bur = burials.get(0);
            rawData.setRawBurialPlace(bur.getPlace() != null ? bur.getPlace().getPlaceName() : null);
            Event existingBurialEvent = burialRepository.findByPerson(person).stream()
                    .filter(b -> b.getBurialEvent() != null)
                    .map(Burial::getBurialEvent).findFirst().orElse(null);
            Event savedBurial = processStandardEvent(bur, person, burialRole, existingBurialEvent, null);
            processGraveAndBurial(savedBurial, person, previousBurialPlace);
        } else {
            if (previousBurialPlace != null) {
                burialRepository.findByPerson(person).forEach(b -> {
                    if(b.getBurialEvent() != null) eventRepository.delete(b.getBurialEvent());
                    burialRepository.delete(b);
                });
                rawData.setRawBurialPlace(null);
            }
        }
        personRepository.save(person);
    }

    private void processFamily(org.gedcom4j.model.Family gedFam, EventTypeParticipantRole weddingRole, UserCode statusMarried, UserCode statusUnknown) {
        String getRefId = gedFam.getXref();
        String uid = GedcomUtils.getCustomFact(gedFam, "_UID");
        Family family = personAndFamilyMatchingStrategy.findExistingFamily(gedFam, uid).orElseGet(() -> {
            Family f = new Family();
            FamilyGedcomData d = new FamilyGedcomData();
            d.setFamily(f); f.setGedcomData(d); return f;
        });

        String husbRef = gedFam.getHusband() != null ? gedFam.getHusband().getIndividual().getXref() : null;
        String wifeRef = gedFam.getWife() != null ? gedFam.getWife().getIndividual().getXref() : null;
        family.setHusband(husbRef != null ? personRepository.findByGedcomDataGedRefId(husbRef).orElse(null) : null);
        family.setWife(wifeRef != null ? personRepository.findByGedcomDataGedRefId(wifeRef).orElse(null) : null);

        UserCode familyStatus = detectFamilyStatus(gedFam);
        family.setStatus(familyStatus != null ? familyStatus : statusUnknown);

        FamilyGedcomData raw = family.getGedcomData();
        raw.setGedRefId(getRefId);
        raw.setGedUid(uid);
        raw.setRawHusbandRefId(husbRef);
        raw.setRawWifeRefId(wifeRef);
        raw.setRawStatus(family.getStatus().getCode());
        raw.setLastImportedAt(LocalDateTime.now());

        family = familyRepository.save(family);

        List<FamilyEvent> marriages = gedFam.getEvents() == null ? Collections.emptyList() :
                gedFam.getEvents().stream().filter(e -> e.getType() == FamilyEventType.MARRIAGE).toList();

        if (!marriages.isEmpty()) {
            org.gedcom4j.model.FamilyEvent marr = marriages.get(0);

            // Only create Wedding event if there is actual data (date or place)
            boolean hasData = (marr.getDate() != null && marr.getDate().getValue() != null) ||
                    (marr.getPlace() != null && marr.getPlace().getPlaceName() != null);

            if (hasData) {
                raw.setRawWeddingDate(marr.getDate() != null ? marr.getDate().getValue() : null);
                raw.setRawWeddingPlace(marr.getPlace() != null ? marr.getPlace().getPlaceName() : null);
                Event weddingEvent = processWeddingEvent(marr, family, weddingRole);
                family.setPrimaryWedding(weddingEvent);
            } else if (family.getPrimaryWedding() != null) {
                // Remove blank weddings
                Event oldWedding = family.getPrimaryWedding();
                family.setPrimaryWedding(null);
                raw.setRawWeddingDate(null);
                raw.setRawWeddingPlace(null);
                familyRepository.save(family);
                eventRepository.delete(oldWedding);
            }
        } else if (family.getPrimaryWedding() != null) {
            Event oldWedding = family.getPrimaryWedding();
            family.setPrimaryWedding(null);
            raw.setRawWeddingDate(null);
            raw.setRawWeddingPlace(null);
            familyRepository.save(family);
            eventRepository.delete(oldWedding);
        }

        processFamilyChildren(family, gedFam);
        familyRepository.save(family);
    }

    private void processFamilyChildren(Family family, org.gedcom4j.model.Family gedFam) {
        Map<String, Individual> gedcomChildrenMap = new HashMap<>();
        if (gedFam.getChildren() != null) {
            for (IndividualReference ref : gedFam.getChildren()) {
                gedcomChildrenMap.put(ref.getIndividual().getXref(), ref.getIndividual());
            }
        }

        Iterator<FamilyChild> iterator = family.getChildren().iterator();
        while (iterator.hasNext()) {
            FamilyChild fc = iterator.next();
            String childXref = fc.getChild().getGedcomData().getGedRefId();
            if (!gedcomChildrenMap.containsKey(childXref)) {
                iterator.remove();
                familyChildRepository.delete(fc);
            }
        }
        familyRepository.save(family);
        familyRepository.flush();

        family.getGedcomData().getChildren().clear();
        for (Individual childIndi : gedcomChildrenMap.values()) {
            personRepository.findByGedcomDataGedRefId(childIndi.getXref()).ifPresent(childPerson -> {
                String pedi = extractPedigree(childIndi, gedFam.getXref());
                FamilyChildRelationshipType relType = mapPedigreeToEnum(pedi);

                FamilyChild existingChild = family.getChildren().stream()
                        .filter(fc -> fc.getChild().getId().equals(childPerson.getId()))
                        .findFirst()
                        .orElse(null);

                if (existingChild != null) {
                    existingChild.setRelationshipType(relType);
                } else {
                    FamilyChild fc = new FamilyChild(family, childPerson, relType);
                    family.addChild(fc);
                }

                FamilyChildGedcomData rawChild = new FamilyChildGedcomData();
                rawChild.setRawChildRefId(childIndi.getXref());
                rawChild.setRawRelationshipType(pedi);
                family.getGedcomData().addChild(rawChild);
            });
        }
    }

    private Event processStandardEvent(IndividualEvent gedEvent, Person person, EventTypeParticipantRole role, Event existingEvent, AttributeDefinition attrDef) {
        Event event = existingEvent != null ? existingEvent : new Event();
        event.setEventType(role.getEventType());

        event.setStartDate(null);
        event.setEndDate(null);
        event.setCachedStartSortDateTime(null);
        event.setCachedEndSortDateTime(null);

        if (gedEvent.getDate() != null && gedEvent.getDate().getValue() != null) {
            var parsedDate = dateParser.parse(gedEvent.getDate().getValue());
            event.setStartDate(parsedDate.getStartDate());
            event.setEndDate(parsedDate.getEndDate());
            if (parsedDate.getStartDate() != null) event.setCachedStartSortDateTime(calculateAverageDate(parsedDate.getStartDate()));
            if (parsedDate.getEndDate() != null) event.setCachedEndSortDateTime(calculateAverageDate(parsedDate.getEndDate()));
        }

// Location
        if (gedEvent.getPlace() != null && gedEvent.getPlace().getPlaceName() != null) {
            String placeName = gedEvent.getPlace().getPlaceName();
            Location location = locationRepository.findByName(placeName).orElseGet(() -> locationRepository.save(new Location(placeName)));
            event.setMainLocation(location);
            event = eventRepository.save(event);
            eventRepository.flush();

            Event finalEvent = event;
            EventLocation el = eventLocationRepository.findByEvent(event).orElseGet(() -> {
                EventLocation newEl = new EventLocation();
                newEl.setEvent(finalEvent);
                return newEl;
            });
            el.setLocation(location);
            eventLocationRepository.save(el);
            eventLocationRepository.flush(); // DODAJ TO
        } else {
            event.setMainLocation(null);
            event = eventRepository.save(event);
            eventRepository.flush();
            eventLocationRepository.findByEvent(event).ifPresent(el -> {
                eventLocationRepository.delete(el);
                eventLocationRepository.flush(); // DODAJ TO
            });
        }

        if (existingEvent == null) {
            EventParticipant participant = new EventParticipant(event, person, role);
            eventParticipantRepository.save(participant);
        }

        if (attrDef != null) {
            String attrValue = gedEvent.getCause() != null ? gedEvent.getCause().getValue() : null;
            if (attrValue != null && !attrValue.isBlank()) {
                eventAttrValueRepository.deleteByEvent(event);
                eventAttrValueRepository.flush();
                saveEventAttribute(event, attrDef, attrValue);
            } else {
                eventAttrValueRepository.deleteByEvent(event);
            }
        }
        return event;
    }

    private Event processWeddingEvent(org.gedcom4j.model.FamilyEvent marr, Family family, EventTypeParticipantRole role) {
        Event event = family.getPrimaryWedding() != null ? family.getPrimaryWedding() : new Event();
        event.setEventType(role.getEventType());

        if (marr.getDate() != null && marr.getDate().getValue() != null) {
            var parsedDate = dateParser.parse(marr.getDate().getValue());
            event.setStartDate(parsedDate.getStartDate());
            event.setEndDate(parsedDate.getEndDate());
            if (parsedDate.getStartDate() != null) event.setCachedStartSortDateTime(calculateAverageDate(parsedDate.getStartDate()));
            if (parsedDate.getEndDate() != null) event.setCachedEndSortDateTime(calculateAverageDate(parsedDate.getEndDate()));
        } else {
            event.setStartDate(null); event.setEndDate(null); event.setCachedStartSortDateTime(null); event.setCachedEndSortDateTime(null);
        }

// Location
        if (marr.getPlace() != null && marr.getPlace().getPlaceName() != null) {
            String placeName = marr.getPlace().getPlaceName();
            Location loc = locationRepository.findByName(placeName).orElseGet(() -> locationRepository.save(new Location(placeName)));
            event.setMainLocation(loc);

            event = eventRepository.save(event);
            eventRepository.flush();

            Event finalEvent = event;
            EventLocation el = eventLocationRepository.findByEvent(event).orElseGet(() -> {
                EventLocation newEl = new EventLocation();
                newEl.setEvent(finalEvent);
                return newEl;
            });
            el.setLocation(loc);
            eventLocationRepository.save(el);
            eventLocationRepository.flush(); // DODAJ TO
        } else {
            event.setMainLocation(null);
            event = eventRepository.save(event);
            eventRepository.flush();
            eventLocationRepository.findByEvent(event).ifPresent(el -> {
                eventLocationRepository.delete(el);
                eventLocationRepository.flush(); // DODAJ TO
            });
        }

        if (family.getHusband() != null) addParticipantIfNotExists(event, family.getHusband(), role);
        if (family.getWife() != null) addParticipantIfNotExists(event, family.getWife(), role);

        return eventRepository.save(event);
    }

    private UserCode detectFamilyStatus(org.gedcom4j.model.Family gedFam) {
        List<FamilyEvent> famEvents = gedFam.getEvents() != null ? gedFam.getEvents() : Collections.emptyList();

        // 1. Niestandardowe MyHeritage oraz standardowe zdarzenia zdefiniowane jako TYPE wewnątrz EVEN LUB MARR
        for (FamilyEvent event : famEvents) {
            // W MyHeritage 2 TYPE może być wewnątrz 1 EVEN albo 1 MARR
            if ((event.getType() == FamilyEventType.EVENT || event.getType() == FamilyEventType.MARRIAGE)
                    && event.getSubType() != null) {

                String typeValue = event.getSubType().getValue();
                if (typeValue != null) {
                    if (typeValue.startsWith("MYHERITAGE:REL_")) {
                        String statusSuffix = typeValue.substring("MYHERITAGE:REL_".length());
                        try {
                            return findSystemCode(CodeCategory.FAMILY_STATUS.name(), statusSuffix);
                        } catch (RuntimeException e) { /* ignorujemy nieznane */ }
                    } else if (typeValue.equalsIgnoreCase("Separation")) {
                        return findSystemCode(CodeCategory.FAMILY_STATUS.name(), FamilyStatus.SEPARATED.name());
                    } else if (typeValue.equalsIgnoreCase("Death of Spouse")) {
                        return findSystemCode(CodeCategory.FAMILY_STATUS.name(), FamilyStatus.WIDOWED.name());
                    }
                }
            }
        }

        // 2. Standardowe typy zdarzeń (DIV, ANUL, ENGA, MARR)
        if (famEvents.stream().anyMatch(e -> e.getType() == FamilyEventType.DIVORCE))
            return findSystemCode(CodeCategory.FAMILY_STATUS.name(), FamilyStatus.DIVORCED.name());
        if (famEvents.stream().anyMatch(e -> e.getType() == FamilyEventType.ANNULMENT))
            return findSystemCode(CodeCategory.FAMILY_STATUS.name(), FamilyStatus.ANNULLED.name());
        if (famEvents.stream().anyMatch(e -> e.getType() == FamilyEventType.ENGAGEMENT))
            return findSystemCode(CodeCategory.FAMILY_STATUS.name(), FamilyStatus.ENGAGED.name());
        if (famEvents.stream().anyMatch(e -> e.getType() == FamilyEventType.MARRIAGE))
            return findSystemCode(CodeCategory.FAMILY_STATUS.name(), FamilyStatus.MARRIED.name());

        return findSystemCode(CodeCategory.FAMILY_STATUS.name(), FamilyStatus.UNKNOWN.name());
    }

    private String extractPedigree(Individual child, String familyXref) {
        if (child.getFamiliesWhereChild() != null) {
            for (org.gedcom4j.model.FamilyChild link : child.getFamiliesWhereChild()) {
                if (link.getFamily() != null && link.getFamily().getXref().equals(familyXref)) {
                    if (link.getPedigree() != null) {
                        return link.getPedigree().getValue();
                    }
                }
            }
        }
        return null;
    }

    private FamilyChildRelationshipType mapPedigreeToEnum(String pedi) {
        if (pedi == null) return FamilyChildRelationshipType.BIOLOGICAL;
        String clean = pedi.trim().toUpperCase();
        if (clean.equals("ADOPTED")) return FamilyChildRelationshipType.ADOPTED;
        if (clean.equals("FOSTER")) return FamilyChildRelationshipType.FOSTER;
        return FamilyChildRelationshipType.BIOLOGICAL;
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
            Burial burial = burialRepository.findByPerson(person).stream()
                    .filter(b -> b.getBurialEvent() != null && b.getBurialEvent().getId().equals(burialEvent.getId()))
                    .findFirst()
                    .orElseGet(Burial::new);

            // Jeśli grób był w innej lokacji, lub to nowy pochówek, stwórzmy nowy Grób.
            if (burial.getGrave() == null || previousBurialPlace == null || !burialEvent.getMainLocation().getName().equals(previousBurialPlace)) {
                Grave grave = new Grave();
                grave.setCemeteryLocation(burialEvent.getMainLocation());
                grave = graveRepository.save(grave);
                burial.setGrave(grave);
            } else {
                // Mimo to upewnijmy się, że ma przypisaną obecną lokację
                burial.getGrave().setCemeteryLocation(burialEvent.getMainLocation());
                graveRepository.save(burial.getGrave());
            }

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
                        String.format("Missing configuration: %s - %s", type.getCode(), role.getCode())));
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
        if (fullName == null || fullName.isEmpty()) return new NameParts("", "");
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