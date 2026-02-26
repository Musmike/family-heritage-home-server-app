package com.musmike.familyheritage.integration.service;

import com.musmike.familyheritage.integration.AbstractIntegrationTest;
import com.musmike.familyheritage.model.*;
import com.musmike.familyheritage.model.enums.*;
import com.musmike.familyheritage.repository.*;
import com.musmike.familyheritage.service.GedcomImportService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class GedcomImportServiceTest extends AbstractIntegrationTest {
    @Autowired private GedcomImportService importService;
    @Autowired private PersonRepository personRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private BurialRepository burialRepository;
    @Autowired private EventAttributeValueRepository eventAttrRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private EventLocationRepository eventLocationRepository;
    @Autowired private PersonNameRepository personNameRepository;
    @Autowired private FamilyRepository familyRepository;
    @Autowired private LocationRepository locationRepository;
    @Autowired private AttributeDictionaryValueRepository attrDictValueRepository;
    @Autowired private GraveRepository graveRepository;

    // Importing the people
    @Test
    @Transactional
    void shouldImportFullPersonLifecycle_VerifyAllFields() throws Exception {
        // GIVEN
        String gedcomContent = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID 581e20c218f061e69c6788ae1d62490a
            1 NAME Jan /Kowalski/
            1 SEX M
            1 BIRT
            2 DATE 10 JAN 1920
            2 PLAC Warszawa, Polska
            1 DEAT
            2 DATE 15 FEB 1990
            2 PLAC Kraków, Polska
            2 CAUS Zawał serca
            1 BURI
            2 PLAC Cmentarz Rakowicki, Kraków
            0 TRLR
            """;

        ByteArrayInputStream inputStream = new ByteArrayInputStream(gedcomContent.getBytes(StandardCharsets.UTF_8));

        // WHEN
        importService.importGedcom(inputStream);

        entityManager.flush();
        entityManager.clear();

        // THEN
        List<Person> persons = personRepository.findAll();
        assertThat(persons).hasSize(1);
        Person person = persons.getFirst();

        // Raw Data
        PersonGedcomData raw = person.getGedcomData();
        assertThat(raw.getGedUid()).isEqualTo("581e20c218f061e69c6788ae1d62490a");
        assertThat(raw.getGedRefId()).isEqualTo("@I1@");
        assertThat(raw.getRawGivenName()).isEqualTo("Jan");
        assertThat(raw.getRawSurname()).isEqualTo("Kowalski");
        assertThat(raw.getRawSex()).isEqualTo("M");
        assertThat(raw.getRawDeathIndicated()).isTrue();
        assertThat(raw.getRawBirthDate()).isEqualTo("10 JAN 1920");
        assertThat(raw.getRawBirthPlace()).isEqualTo("Warszawa, Polska");
        assertThat(raw.getRawDeathDate()).isEqualTo("15 FEB 1990");
        assertThat(raw.getRawDeathPlace()).isEqualTo("Kraków, Polska");
        assertThat(raw.getRawCauseOfDeath()).isEqualTo("Zawał serca");
        assertThat(raw.getRawBurialPlace()).isEqualTo("Cmentarz Rakowicki, Kraków");
        assertThat(raw.getLastImportedAt()).isNotNull();


        // Local Data
        assertThat(person.getCachedGivenName()).isEqualTo("Jan");
        assertThat(person.getCachedSurname()).isEqualTo("Kowalski");
        assertThat(person.getSex()).isEqualTo(PersonSex.MALE);
        assertThat(person.getLifeState()).isEqualTo(LifeState.DECEASED);

        List<PersonName> names = personNameRepository.findAll();
        assertThat(names).hasSize(1);
        PersonName nameRecord = names.getFirst();
        assertThat(nameRecord.getPerson().getId()).isEqualTo(person.getId());
        assertThat(nameRecord.getType()).isEqualTo(EventType.BIRTH.name());
        assertThat(nameRecord.getGivenName()).isEqualTo("Jan");
        assertThat(nameRecord.getSurname()).isEqualTo("Kowalski");
        assertThat(nameRecord.getIsCurrent()).isTrue();


        List<Event> events = eventRepository.findAll();
        assertThat(events).hasSize(3);

        // Birth Event
        Event birth = person.getBirthEvent();
        assertThat(birth).isNotNull();
        assertThat(birth.getEventType().getCode()).isEqualTo(EventType.BIRTH.name());
        assertThat(birth.getCachedStartSortDateTime())
                .hasYear(1920)
                .hasMonth(Month.of(1))
                .hasDayOfMonth(10);
        assertThat(birth.getCachedEndSortDateTime()).isNull();
        assertThat(birth.getStartDate().getStartYear()).isEqualTo(1920);
        assertThat(birth.getStartDate().getStartMonth()).isEqualTo(1);
        assertThat(birth.getStartDate().getStartDay()).isEqualTo(10);
        assertThat(birth.getStartDate()).isNotNull();
        assertThat(birth.getEndDate()).isNull();
        assertThat(birth.getMainLocation().getName()).isEqualTo("Warszawa, Polska");

        List<EventLocation> birthLocs = eventLocationRepository.findAll().stream()
                .filter(el -> el.getEvent().getId().equals(birth.getId()))
                .toList();
        assertThat(birthLocs).hasSize(1);
        assertThat(birthLocs.getFirst().getLocation().getName()).isEqualTo("Warszawa, Polska");


        List<EventParticipant> birthParticipants = birth.getParticipants().stream().toList();
        assertThat(birthParticipants.getFirst().getRole().getParticipantRole().getCode())
            .isEqualTo(EventParticipantRole.PRINCIPAL.name());


        // Death Event
        Event death = person.getDeathEvent();
        assertThat(death).isNotNull();
        assertThat(death.getEventType().getCode()).isEqualTo(EventType.DEATH.name());
        assertThat(death.getCachedStartSortDateTime())
                .hasYear(1990)
                .hasMonth(Month.of(2))
                .hasDayOfMonth(15);
        assertThat(death.getCachedEndSortDateTime()).isNull();
        assertThat(death.getStartDate()).isNotNull();
        assertThat(death.getStartDate().getStartYear()).isEqualTo(1990);
        assertThat(death.getStartDate().getStartMonth()).isEqualTo(2);
        assertThat(death.getStartDate().getStartDay()).isEqualTo(15);
        assertThat(death.getEndDate()).isNull();
        assertThat(death.getMainLocation().getName()).isEqualTo("Kraków, Polska");

        List<EventLocation> deathLocs = eventLocationRepository.findAll().stream()
                .filter(el -> el.getEvent().getId().equals(death.getId()))
                .toList();
        assertThat(deathLocs).hasSize(1);
        assertThat(deathLocs.getFirst().getLocation().getName()).isEqualTo("Kraków, Polska");

        List<EventParticipant> deathParticipants = death.getParticipants().stream().toList();
        assertThat(deathParticipants.getFirst().getRole().getParticipantRole().getCode())
            .isEqualTo(EventParticipantRole.PRINCIPAL.name());

        List<EventAttributeValue> deathAttrs = eventAttrRepository.findAll().stream()
                .filter(eav -> eav.getEvent().getId().equals(death.getId()))
                .toList();
        assertThat(deathAttrs).hasSize(1);
        AttributeDictionaryValue attrVal = deathAttrs.getFirst().getAttributeDictionaryValue();
        assertThat(attrVal.getCode()).isEqualTo("Zawał serca");
        assertThat(attrVal.getAttributeDefinition().getCode())
                .isEqualTo(DeathEventAttributeDefinition.CAUSE_OF_DEATH.name());


        // Burial Event
        List<Burial> burials = burialRepository.findAll();
        assertThat(burials).hasSize(1);
        Burial burialRecord = burials.getFirst();

        Event burialEvent = burialRecord.getBurialEvent();
        assertThat(burialEvent).isNotNull();
        assertThat(burialEvent.getEventType().getCode()).isEqualTo(EventType.BURIAL.name());
        assertThat(burialEvent.getCachedStartSortDateTime()).isNull();
        assertThat(burialEvent.getCachedEndSortDateTime()).isNull();
        assertThat(burialEvent.getStartDate()).isNull();
        assertThat(burialEvent.getEndDate()).isNull();
        assertThat(burialEvent.getMainLocation().getName()).isEqualTo("Cmentarz Rakowicki, Kraków");

        assertThat(burialRecord.getGrave()).isNotNull();
        assertThat(burialRecord.getGrave().getCemeteryLocation().getName())
                .isEqualTo("Cmentarz Rakowicki, Kraków");
        assertThat(burialRecord.getPerson().getId()).isEqualTo(person.getId());
        assertThat(burialRecord.getIsCurrent()).isTrue();

        List<EventLocation> burialLocs = eventLocationRepository.findAll().stream()
                .filter(el -> el.getEvent().getId().equals(burialEvent.getId()))
                .toList();
        assertThat(burialLocs).hasSize(1);
        assertThat(burialLocs.getFirst().getLocation().getName()).isEqualTo("Cmentarz Rakowicki, Kraków");

        List<EventParticipant> burialParticipants = burialEvent.getParticipants().stream().toList();
        assertThat(burialParticipants.getFirst().getRole().getParticipantRole().getCode())
            .isEqualTo(EventParticipantRole.PRINCIPAL.name());
    }

    // Importing the families
    @Test
    @Transactional
    void shouldImportThreeFamiliesWithChildrenWeddingEventsAndGedcomData() throws Exception {
        // GIVEN
        String gedcomContent = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 NAME John /Smith/
            1 SEX M
            1 FAMS @F1@
            0 @I2@ INDI
            1 NAME Lucy /Johnson/
            1 SEX F
            1 FAMS @F1@
            0 @I3@ INDI
            1 NAME Mark /Smith/
            1 FAMS @F3@
            1 FAMC @F1@
            0 @I4@ INDI
            1 NAME Karol /Krawczyk/
            1 FAMS @F2@
            1 FAMC @F1@
            2 PEDI Adopted
            0 @I5@ INDI
            1 NAME Zofia /Nowak/
            1 FAMS @F2@
            0 @F1@ FAM
            1 _UID 48152353522421
            1 HUSB @I1@
            1 WIFE @I2@
            1 CHIL @I3@
            1 CHIL @I4@
            1 MARR
            2 DATE 10 JUN 1950
            2 PLAC Warszawa
            1 DIV
            0 @F2@ FAM
            1 _UID 78152d353522421
            1 HUSB @I4@
            1 WIFE @I5@
            1 MARR
            2 DATE 5 JAN 1995
            2 PLAC Kraków
            2 TYPE MYHERITAGE:REL_PARTNERS
            0 @F3@ FAM
            1 HUSB @I3@
            1 MARR
            0 TRLR
            """;

        // WHEN
        ByteArrayInputStream inputStream = new ByteArrayInputStream(gedcomContent.getBytes(StandardCharsets.UTF_8));
        importService.importGedcom(inputStream);

        entityManager.flush();
        entityManager.clear();

        // THEN
        List<Family> families = familyRepository.findAll();
        assertThat(families).hasSize(3);

        Family fam1 = familyRepository
                .findByGedcomDataGedRefId("@F1@")
                .orElseThrow();
        Family fam2 = familyRepository
                .findByGedcomDataGedRefId("@F2@")
                .orElseThrow();
        Family fam3 = familyRepository
                .findByGedcomDataGedRefId("@F3@")
                .orElseThrow();

        // =========================================================
        // FAMILY 1 – John Smith + Lucy Johnson Marriage
        // =========================================================

        // Spouses
        assertThat(fam1.getHusband()).isNotNull();
        assertThat(fam1.getHusband().getCachedGivenName()).isEqualTo("John");
        assertThat(fam1.getHusband().getCachedSurname()).isEqualTo("Smith");
        assertThat(fam1.getWife()).isNotNull();
        assertThat(fam1.getWife().getCachedGivenName()).isEqualTo("Lucy");
        assertThat(fam1.getWife().getCachedSurname()).isEqualTo("Johnson");

        // Wedding Event
        assertThat(fam1.getPrimaryWedding()).isNotNull();
        Event wedding1 = fam1.getPrimaryWedding();
        assertThat(wedding1.getEventType().getCode()).isEqualTo(EventType.WEDDING.name());
        assertThat(wedding1.getMainLocation().getName()).isEqualTo("Warszawa");
        assertThat(wedding1.getStartDate()).isNotNull();
        assertThat(wedding1.getStartDate().getStartYear()).isEqualTo(1950);
        assertThat(wedding1.getStartDate().getStartMonth()).isEqualTo(6);
        assertThat(wedding1.getStartDate().getStartDay()).isEqualTo(10);
        assertThat(wedding1.getEndDate()).isNull();
        assertThat(wedding1.getCachedStartSortDateTime())
                .hasYear(1950)
                .hasMonth(Month.of(6))
                .hasDayOfMonth(10);

        // Event Locations
        List<EventLocation> weddingLocs1 = eventLocationRepository.findAll().stream()
                .filter(el -> el.getEvent().getId().equals(wedding1.getId()))
                .toList();
        assertThat(weddingLocs1).hasSize(1);
        assertThat(weddingLocs1.getFirst().getLocation().getName()).isEqualTo("Warszawa");

        // Event Participants
        assertThat(wedding1.getParticipants()).hasSize(2);
        assertThat(wedding1.getParticipants())
                .extracting(ep -> ep.getPerson().getCachedGivenName())
                .containsExactlyInAnyOrder("John", "Lucy");
        assertThat(wedding1.getParticipants())
                .allMatch(ep -> ep.getRole().getParticipantRole().getCode()
                        .equals(EventParticipantRole.PRINCIPAL.name()));

        // Status
        assertThat(fam1.getStatus().getCode()).isEqualTo(FamilyStatus.DIVORCED.name());

        // Children
        assertThat(fam1.getChildren()).hasSize(2);
        assertThat(fam1.getChildren())
                .extracting(fc -> fc.getChild().getCachedGivenName())
                .containsExactlyInAnyOrder("Mark", "Karol");

        FamilyChild markRel = fam1.getChildren().stream()
                .filter(fc -> fc.getChild().getCachedGivenName().equals("Mark"))
                .findFirst().orElseThrow();
        assertThat(markRel.getRelationshipType()).isEqualTo(FamilyChildRelationshipType.BIOLOGICAL);

        FamilyChild karolRel = fam1.getChildren().stream()
                .filter(fc -> fc.getChild().getCachedGivenName().equals("Karol"))
                .findFirst().orElseThrow();
        assertThat(karolRel.getRelationshipType()).isEqualTo(FamilyChildRelationshipType.ADOPTED);

        // Raw Gedcom Data
        FamilyGedcomData gd1 = fam1.getGedcomData();
        assertThat(gd1.getGedRefId()).isEqualTo("@F1@");
        assertThat(gd1.getGedUid()).isEqualTo("48152353522421");
        assertThat(gd1.getRawHusbandRefId()).isEqualTo("@I1@");
        assertThat(gd1.getRawWifeRefId()).isEqualTo("@I2@");
        assertThat(gd1.getRawWeddingDate()).isEqualTo("10 JUN 1950");
        assertThat(gd1.getRawWeddingPlace()).isEqualTo("Warszawa");
        assertThat(gd1.getRawStatus()).isEqualTo(FamilyStatus.DIVORCED.name());
        assertThat(gd1.getLastImportedAt()).isNotNull();
        assertThat(gd1.getLastImportedAt()).isBefore(LocalDateTime.now());

        // Raw Gedcom Data - Children
        Set<FamilyChildGedcomData> rawChildren1 = gd1.getChildren();
        assertThat(rawChildren1).hasSize(2);
        assertThat(rawChildren1)
                .extracting(FamilyChildGedcomData::getRawChildRefId)
                .containsExactlyInAnyOrder("@I3@", "@I4@");

        FamilyChildGedcomData rawMark = rawChildren1.stream()
                .filter(c -> c.getRawChildRefId().equals("@I3@"))
                .findFirst().orElseThrow();
        assertThat(rawMark.getRawRelationshipType()).isNull();

        FamilyChildGedcomData rawKarol = rawChildren1.stream()
                .filter(c -> c.getRawChildRefId().equals("@I4@"))
                .findFirst().orElseThrow();
        assertThat(rawKarol.getRawRelationshipType()).isEqualToIgnoringCase("Adopted");


        // =========================================================
        // FAMILY 2 – Karol Krawczyk + Zofia Nowak Partnership
        // =========================================================

        // Spouses
        assertThat(fam2.getHusband()).isNotNull();
        assertThat(fam2.getHusband().getCachedGivenName()).isEqualTo("Karol");
        assertThat(fam2.getWife()).isNotNull();
        assertThat(fam2.getWife().getCachedGivenName()).isEqualTo("Zofia");

        // Children
        assertThat(fam2.getChildren()).isEmpty();

        // Wedding Event
        assertThat(fam2.getPrimaryWedding()).isNotNull();
        Event wedding2 = fam2.getPrimaryWedding();
        assertThat(wedding2.getEventType().getCode()).isEqualTo(EventType.WEDDING.name());
        assertThat(wedding2.getMainLocation().getName()).isEqualTo("Kraków");
        assertThat(wedding2.getStartDate()).isNotNull();
        assertThat(wedding2.getStartDate().getStartYear()).isEqualTo(1995);
        assertThat(wedding2.getStartDate().getStartMonth()).isEqualTo(1);
        assertThat(wedding2.getStartDate().getStartDay()).isEqualTo(5);

        // Event Locations
        List<EventLocation> weddingLocs2 = eventLocationRepository.findAll().stream()
                .filter(el -> el.getEvent().getId().equals(wedding2.getId()))
                .toList();
        assertThat(weddingLocs2).hasSize(1);
        assertThat(weddingLocs2.getFirst().getLocation().getName()).isEqualTo("Kraków");

        // Event Participants
        assertThat(wedding2.getParticipants()).hasSize(2);
        assertThat(wedding2.getParticipants())
                .extracting(ep -> ep.getPerson().getCachedGivenName())
                .containsExactlyInAnyOrder("Karol", "Zofia");
        assertThat(wedding2.getParticipants())
                .allMatch(ep -> ep.getRole().getParticipantRole().getCode()
                        .equals(EventParticipantRole.PRINCIPAL.name()));

        // Status
        assertThat(fam2.getStatus().getCode()).isEqualTo(FamilyStatus.PARTNERS.name());

        // Raw Gedcom Data
        FamilyGedcomData gd2 = fam2.getGedcomData();
        assertThat(gd2.getGedRefId()).isEqualTo("@F2@");
        assertThat(gd2.getGedUid()).isEqualTo("78152d353522421");
        assertThat(gd2.getRawHusbandRefId()).isEqualTo("@I4@");
        assertThat(gd2.getRawWifeRefId()).isEqualTo("@I5@");
        assertThat(gd2.getRawWeddingDate()).isEqualTo("5 JAN 1995");
        assertThat(gd2.getRawWeddingPlace()).isEqualTo("Kraków");
        assertThat(gd2.getRawStatus()).isEqualTo(FamilyStatus.PARTNERS.name());
        assertThat(gd2.getLastImportedAt()).isNotNull();

        // Raw Gedcom Data Children
        assertThat(gd2.getChildren()).isEmpty();

        // Karol is both a husband in F2 and a child in F1 – cross-family verification
        assertThat(fam2.getHusband().getId()).isEqualTo(karolRel.getChild().getId());


        // =========================================================
        // FAMILY 3 – Mark Smith (no wife, no wedding date, default status)
        // =========================================================

        // Spouses
        assertThat(fam3.getHusband()).isNotNull();
        assertThat(fam3.getHusband().getCachedGivenName()).isEqualTo("Mark");
        assertThat(fam3.getHusband().getCachedSurname()).isEqualTo("Smith");
        assertThat(fam3.getWife()).isNull();

        // Status – brak tagów DIV/SEP/etc., powinien ustawić się domyślny MARRIED
        assertThat(fam3.getStatus().getCode()).isEqualTo(FamilyStatus.MARRIED.name());

        // Brak ślubu i dzieci
        assertThat(fam3.getPrimaryWedding()).isNull();
        assertThat(fam3.getChildren()).isEmpty();

        // Raw Gedcom Data
        FamilyGedcomData gd3 = fam3.getGedcomData();
        assertThat(gd3.getGedRefId()).isEqualTo("@F3@");
        assertThat(gd3.getGedUid()).isNull();
        assertThat(gd3.getRawHusbandRefId()).isEqualTo("@I3@");
        assertThat(gd3.getRawWifeRefId()).isNull();
        assertThat(gd3.getRawWeddingDate()).isNull();
        assertThat(gd3.getRawWeddingPlace()).isNull();
        assertThat(gd3.getRawStatus()).isEqualTo(FamilyStatus.MARRIED.name());
        assertThat(gd3.getLastImportedAt()).isNotNull();
        assertThat(gd3.getChildren()).isEmpty();

        // Mark is both a husband in F3 and a child in F1 – cross-family verification
        assertThat(fam3.getHusband().getId()).isEqualTo(markRel.getChild().getId());
    }

    // Updating the person data during the second import
    @Test
    @Transactional
    void shouldUpdatePersonFieldsOnSecondImport() throws Exception {
        // GIVEN
        String gedcomV1 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID 581e20c218f061e69c6788ae1d62490a
            1 NAME Jan /Kowalski/
            1 SEX M
            1 BIRT
            2 DATE 10 JAN 1920
            2 PLAC Warszawa, Polska
            1 DEAT
            2 DATE 15 FEB 1990
            2 PLAC Kraków, Polska
            2 CAUS Zawał serca
            1 BURI
            2 PLAC Cmentarz Rakowicki, Kraków
            0 TRLR
            """;

        ByteArrayInputStream inputStreamV1 = new ByteArrayInputStream(gedcomV1.getBytes(StandardCharsets.UTF_8));
        importService.importGedcom(inputStreamV1);

        Person p1 = personRepository.findAll().getFirst();
        assertThat(p1.getCachedGivenName()).isEqualTo("Jan");
        assertThat(p1.getSex()).isEqualTo(PersonSex.MALE);
        assertThat(p1.getBirthEvent().getStartDate().getStartYear()).isEqualTo(1920);
        assertThat(p1.getBirthEvent().getMainLocation().getName()).isEqualTo("Warszawa, Polska");
        assertThat(p1.getGedcomData().getRawCauseOfDeath()).isEqualTo("Zawał serca");
        assertThat(burialRepository.findAll().getFirst().getGrave().getCemeteryLocation()
                .getName()).isEqualTo("Cmentarz Rakowicki, Kraków");

        // WHEN
        String gedcomV2 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I2@ INDI
            1 _UID 581e20c218f061e69c6788ae1d62490a
            1 NAME Tomasz /Nowak/
            1 SEX F
            1 BIRT
            2 DATE 15 MAR 1925
            2 PLAC Gdańsk
            1 DEAT
            2 DATE 2000
            2 PLAC Sopot
            2 CAUS Wypadek
            1 BURI
            2 PLAC Cmentarz Nowy w Warszawie
            0 TRLR
            """;

        LocalDateTime firstImportTime = p1.getGedcomData().getLastImportedAt();
        Thread.sleep(100);

        ByteArrayInputStream inputStreamV2 = new ByteArrayInputStream(gedcomV2.getBytes(StandardCharsets.UTF_8));
        importService.importGedcom(inputStreamV2);

        entityManager.flush();
        entityManager.clear();

        // THEN
        List<Person> persons = personRepository.findAll();
        assertThat(persons).hasSize(1);
        Person p2 = persons.getFirst();

        // Raw Data
        PersonGedcomData raw = p2.getGedcomData();
        assertThat(raw.getGedRefId()).isEqualTo("@I2@");
        assertThat(raw.getRawGivenName()).isEqualTo("Tomasz");
        assertThat(raw.getRawSurname()).isEqualTo("Nowak");
        assertThat(raw.getRawSex()).isEqualTo("F");
        assertThat(raw.getRawDeathIndicated()).isTrue();
        assertThat(raw.getRawBirthDate()).isEqualTo("15 MAR 1925");
        assertThat(raw.getRawBirthPlace()).isEqualTo("Gdańsk");
        assertThat(raw.getRawDeathDate()).isEqualTo("2000");
        assertThat(raw.getRawDeathPlace()).isEqualTo("Sopot");
        assertThat(raw.getRawCauseOfDeath()).isEqualTo("Wypadek");
        assertThat(raw.getRawBurialPlace()).isEqualTo("Cmentarz Nowy w Warszawie");
        assertThat(raw.getLastImportedAt()).isNotEqualTo(firstImportTime);

        // Local Data
        assertThat(p2.getCachedGivenName()).isEqualTo("Tomasz");
        assertThat(p2.getCachedSurname()).isEqualTo("Nowak");
        assertThat(p2.getSex()).isEqualTo(PersonSex.FEMALE);

        List<PersonName> updatedNames = personNameRepository.findAll();
        assertThat(updatedNames).hasSize(1);
        PersonName updatedName = updatedNames.getFirst();
        assertThat(updatedName.getPerson().getId()).isEqualTo(p2.getId());
        assertThat(updatedName.getType()).isEqualTo(EventType.BIRTH.name());
        assertThat(updatedName.getGivenName()).isEqualTo("Tomasz");
        assertThat(updatedName.getSurname()).isEqualTo("Nowak");
        assertThat(updatedName.getIsCurrent()).isTrue();

        List<Event> events = eventRepository.findAll();
        assertThat(events).hasSize(3);

        // Birth Event
        Event birth = p2.getBirthEvent();
        assertThat(birth).isNotNull();
        assertThat(birth.getCachedStartSortDateTime())
                .hasYear(1925)
                .hasMonth(Month.of(3))
                .hasDayOfMonth(15);
        assertThat(birth.getCachedEndSortDateTime()).isNull();
        assertThat(birth.getStartDate()).isNotNull();
        assertThat(birth.getStartDate().getStartYear()).isEqualTo(1925);
        assertThat(birth.getStartDate().getStartMonth()).isEqualTo(3);
        assertThat(birth.getStartDate().getStartDay()).isEqualTo(15);
        assertThat(birth.getEndDate()).isNull();
        assertThat(birth.getMainLocation().getName()).isEqualTo("Gdańsk");

        List<EventLocation> birthLocs = eventLocationRepository.findAll().stream()
                .filter(el -> el.getEvent().getId().equals(birth.getId()))
                .toList();
        assertThat(birthLocs).hasSize(1);
        assertThat(birthLocs.getFirst().getLocation().getName()).isEqualTo("Gdańsk");


        List<EventParticipant> birthParticipants = birth.getParticipants().stream().toList();
        assertThat(birthParticipants.getFirst().getRole().getParticipantRole().getCode())
                .isEqualTo(EventParticipantRole.PRINCIPAL.name());


        // Death Event
        Event death = p2.getDeathEvent();
        assertThat(death).isNotNull();
        assertThat(death.getEventType().getCode()).isEqualTo(EventType.DEATH.name());
        assertThat(death.getCachedStartSortDateTime()).hasYear(2000);
        assertThat(death.getCachedEndSortDateTime()).isNull();
        assertThat(death.getStartDate()).isNotNull();
        assertThat(death.getStartDate().getStartYear()).isEqualTo(2000);
        assertThat(death.getStartDate().getStartMonth()).isNull();
        assertThat(death.getStartDate().getStartDay()).isNull();
        assertThat(death.getEndDate()).isNull();
        assertThat(death.getMainLocation().getName()).isEqualTo("Sopot");

        List<EventLocation> deathLocs = eventLocationRepository.findAll().stream()
                .filter(el -> el.getEvent().getId().equals(death.getId()))
                .toList();
        assertThat(deathLocs).hasSize(1);
        assertThat(deathLocs.getFirst().getLocation().getName()).isEqualTo("Sopot");

        List<EventParticipant> deathParticipants = death.getParticipants().stream().toList();
        assertThat(deathParticipants.getFirst().getRole().getParticipantRole().getCode())
                .isEqualTo(EventParticipantRole.PRINCIPAL.name());

        List<EventAttributeValue> deathAttrs = eventAttrRepository.findAll().stream()
                .filter(eav -> eav.getEvent().getId().equals(death.getId()))
                .toList();
        assertThat(deathAttrs).hasSize(1);
        AttributeDictionaryValue attrVal = deathAttrs.getFirst().getAttributeDictionaryValue();
        assertThat(attrVal.getCode()).isEqualTo("Wypadek");
        assertThat(attrVal.getAttributeDefinition().getCode())
                .isEqualTo(DeathEventAttributeDefinition.CAUSE_OF_DEATH.name());


        // Burial Event
        List<Burial> burials = burialRepository.findAll();
        assertThat(burials).hasSize(1);
        Burial burialRecord = burials.getFirst();

        Event burialEvent = burialRecord.getBurialEvent();
        assertThat(burialEvent).isNotNull();
        assertThat(burialEvent.getEventType().getCode()).isEqualTo(EventType.BURIAL.name());
        assertThat(burialEvent.getCachedStartSortDateTime()).isNull();
        assertThat(burialEvent.getCachedEndSortDateTime()).isNull();
        assertThat(burialEvent.getStartDate()).isNull();
        assertThat(burialEvent.getEndDate()).isNull();
        assertThat(burialEvent.getMainLocation().getName()).isEqualTo("Cmentarz Nowy w Warszawie");

        assertThat(burialRecord.getGrave()).isNotNull();
        assertThat(burialRecord.getGrave().getCemeteryLocation().getName())
                .isEqualTo("Cmentarz Nowy w Warszawie");
        assertThat(burialRecord.getPerson().getId()).isEqualTo(p2.getId());
        assertThat(burialRecord.getIsCurrent()).isTrue();

        List<EventLocation> burialLocs = eventLocationRepository.findAll().stream()
                .filter(el -> el.getEvent().getId().equals(burialEvent.getId()))
                .toList();
        assertThat(burialLocs).hasSize(1);
        assertThat(burialLocs.getFirst().getLocation().getName()).isEqualTo("Cmentarz Nowy w Warszawie");

        List<EventParticipant> burialParticipants = burialEvent.getParticipants().stream().toList();
        assertThat(burialParticipants.getFirst().getRole().getParticipantRole().getCode())
                .isEqualTo(EventParticipantRole.PRINCIPAL.name());
    }

    // Updating the family data during the second import
    @Test
    @Transactional
    void shouldUpdateFamilyFieldsOnSecondImport() throws Exception {
        // GIVEN - first import: F1 has a wedding in Warsaw, status MARRIED, wife Lucy, child Mark
        String gedcomV1 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID aaa-111
            1 NAME John /Smith/
            1 SEX M
            1 FAMS @F1@
            0 @I2@ INDI
            1 _UID bbb-222
            1 NAME Lucy /Johnson/
            1 SEX F
            1 FAMS @F1@
            0 @I3@ INDI
            1 _UID ccc-333
            1 NAME Mark /Smith/
            1 FAMC @F1@
            0 @I4@ INDI
            1 _UID ddd-444
            1 NAME Anna /Smith/
            1 FAMC @F1@
            0 @F1@ FAM
            1 _UID fam-uid-001
            1 HUSB @I1@
            1 WIFE @I2@
            1 CHIL @I3@
            1 CHIL @I4@
            1 MARR
            2 DATE 10 JUN 1950
            2 PLAC Warszawa
            0 TRLR
            """;

        ByteArrayInputStream streamV1 = new ByteArrayInputStream(gedcomV1.getBytes(StandardCharsets.UTF_8));
        importService.importGedcom(streamV1);

        entityManager.flush();
        entityManager.clear();

        // Status verification after the first import
        Family famBefore = familyRepository.findByGedcomDataGedUid("fam-uid-001").orElseThrow();
        LocalDateTime firstImportedAt = famBefore.getGedcomData().getLastImportedAt();
        assertThat(famBefore.getWife().getCachedGivenName()).isEqualTo("Lucy");
        assertThat(famBefore.getGedcomData().getRawWeddingDate()).isEqualTo("10 JUN 1950");
        assertThat(famBefore.getGedcomData().getRawWeddingPlace()).isEqualTo("Warszawa");
        assertThat(famBefore.getChildren()).hasSize(2);

        Thread.sleep(100);

        // WHEN - second import: same family (_UID matches), but different data
        // - changed date and place of marriage
        // - removed wife Lucy (now family without wife)
        // - removed child Anna, only Mark remains
        // - added DIV (divorce)
        String gedcomV2 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID aaa-111
            1 NAME John /Smith/
            1 SEX M
            1 FAMS @F1@
            0 @I2@ INDI
            1 _UID bbb-222
            1 NAME Lucy /Johnson/
            1 SEX F
            0 @I3@ INDI
            1 _UID ccc-333
            1 NAME Mark /Smith/
            1 FAMC @F1@
            0 @F1@ FAM
            1 _UID fam-uid-001
            1 HUSB @I1@
            1 CHIL @I3@
            1 MARR
            2 DATE 5 MAR 1952
            2 PLAC Kraków
            1 DIV
            0 TRLR
            """;

        ByteArrayInputStream streamV2 = new ByteArrayInputStream(gedcomV2.getBytes(StandardCharsets.UTF_8));
        importService.importGedcom(streamV2);

        entityManager.flush();
        entityManager.clear();

        // THEN
        List<Family> families = familyRepository.findAll();
        assertThat(families).hasSize(1);

        Family famAfter = familyRepository.findByGedcomDataGedUid("fam-uid-001").orElseThrow();

        // The record ID has not changed (same object, not new)
        assertThat(famAfter.getId()).isEqualTo(famBefore.getId());

        // My husband remained the same
        assertThat(famAfter.getHusband()).isNotNull();
        assertThat(famAfter.getHusband().getCachedGivenName()).isEqualTo("John");

        // The wife was removed from the family
        assertThat(famAfter.getWife()).isNull();

        // Status updated to DIVORCED
        assertThat(famAfter.getStatus().getCode()).isEqualTo(FamilyStatus.DIVORCED.name());

        // Wedding – updated date and location
        assertThat(famAfter.getPrimaryWedding()).isNotNull();
        Event updatedWedding = famAfter.getPrimaryWedding();
        assertThat(updatedWedding.getStartDate().getStartYear()).isEqualTo(1952);
        assertThat(updatedWedding.getStartDate().getStartMonth()).isEqualTo(3);
        assertThat(updatedWedding.getStartDate().getStartDay()).isEqualTo(5);
        assertThat(updatedWedding.getMainLocation().getName()).isEqualTo("Kraków");

        // Children – only Mark, Anna has been removed
        assertThat(famAfter.getChildren()).hasSize(1);
        assertThat(famAfter.getChildren().iterator().next().getChild().getCachedGivenName()).isEqualTo("Mark");

        // Raw data updated
        FamilyGedcomData gdAfter = famAfter.getGedcomData();
        assertThat(gdAfter.getGedUid()).isEqualTo("fam-uid-001");
        assertThat(gdAfter.getRawWifeRefId()).isNull();
        assertThat(gdAfter.getRawWeddingDate()).isEqualTo("5 MAR 1952");
        assertThat(gdAfter.getRawWeddingPlace()).isEqualTo("Kraków");
        assertThat(gdAfter.getRawStatus()).isEqualTo(FamilyStatus.DIVORCED.name());
        assertThat(gdAfter.getLastImportedAt()).isAfter(firstImportedAt);
        assertThat(gdAfter.getChildren()).hasSize(1);
        assertThat(gdAfter.getChildren().iterator().next().getRawChildRefId()).isEqualTo("@I3@");
    }


    // Removing people who are absent in the second import
    @Test
    @Transactional
    void shouldDeletePersonsAbsentFromSecondImport() throws Exception {
        // GIVEN - import of three persons
        String gedcomV1 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID person-uid-001
            1 NAME Jan /Kowalski/
            1 SEX M
            1 BIRT
            2 DATE 1 JAN 1900
            2 PLAC Warszawa
            0 @I2@ INDI
            1 _UID person-uid-002
            1 NAME Anna /Nowak/
            1 SEX F
            0 @I3@ INDI
            1 _UID person-uid-003
            1 NAME Piotr /Wiśniewski/
            1 SEX M
            0 TRLR
            """;

        ByteArrayInputStream streamV1 = new ByteArrayInputStream(gedcomV1.getBytes(StandardCharsets.UTF_8));
        importService.importGedcom(streamV1);

        entityManager.flush();
        entityManager.clear();

        assertThat(personRepository.findAll()).hasSize(3);
        Person jan = personRepository.findByGedcomDataGedUid("person-uid-001").orElseThrow();
        Long janId = jan.getId();

        // WHEN - the second import contains only I1 and I2 (Peter was removed)
        String gedcomV2 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID person-uid-001
            1 NAME Jan /Kowalski/
            1 SEX M
            1 BIRT
            2 DATE 1 JAN 1900
            2 PLAC Warszawa
            0 @I2@ INDI
            1 _UID person-uid-002
            1 NAME Anna /Nowak/
            1 SEX F
            0 TRLR
            """;

        ByteArrayInputStream streamV2 = new ByteArrayInputStream(gedcomV2.getBytes(StandardCharsets.UTF_8));
        importService.importGedcom(streamV2);
        entityManager.flush();
        entityManager.clear();

        // // THEN - only 2 people in the database, Piotr removed
        List<Person> remaining = personRepository.findAll();
        assertThat(remaining).hasSize(2);
        assertThat(remaining)
                .extracting(Person::getCachedGivenName)
                .containsExactlyInAnyOrder("Jan", "Anna");
        assertThat(personRepository.findByGedcomDataGedUid("person-uid-003")).isEmpty();

        // Jan and Anna still exist with correct data
        assertThat(personRepository.findByGedcomDataGedUid("person-uid-001")).isPresent();
        Person janAfter = personRepository.findByGedcomDataGedUid("person-uid-001").orElseThrow();
        assertThat(janAfter.getId()).isEqualTo(janId); // the same record
        assertThat(janAfter.getBirthEvent()).isNotNull();
        assertThat(janAfter.getBirthEvent().getMainLocation().getName()).isEqualTo("Warszawa");
    }


    // Removing families absent in the second import
    @Test
    @Transactional
    void shouldDeleteFamiliesAbsentFromSecondImport() throws Exception {
        // GIVEN - import of two families
        String gedcomV1 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID p-uid-01
            1 NAME John /Smith/
            1 SEX M
            1 FAMS @F1@
            0 @I2@ INDI
            1 _UID p-uid-02
            1 NAME Lucy /Johnson/
            1 SEX F
            1 FAMS @F1@
            0 @I3@ INDI
            1 _UID p-uid-03
            1 NAME Karol /Nowak/
            1 SEX M
            1 FAMS @F2@
            0 @F1@ FAM
            1 _UID fam-del-001
            1 HUSB @I1@
            1 WIFE @I2@
            1 MARR
            2 DATE 1 JAN 1960
            2 PLAC Gdańsk
            0 @F2@ FAM
            1 _UID fam-del-002
            1 HUSB @I3@
            1 MARR
            0 TRLR
            """;

        ByteArrayInputStream streamV1 = new ByteArrayInputStream(gedcomV1.getBytes(StandardCharsets.UTF_8));
        importService.importGedcom(streamV1);
        entityManager.flush();
        entityManager.clear();

        assertThat(familyRepository.findAll()).hasSize(2);
        Family fam1Before = familyRepository.findByGedcomDataGedUid("fam-del-001").orElseThrow();
        Long fam1Id = fam1Before.getId();
        Long weddingEventId = fam1Before.getPrimaryWedding().getId();

        // WHEN - The second import contains only F1 (F2 has disappeared), people remain.
        String gedcomV2 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID p-uid-01
            1 NAME John /Smith/
            1 SEX M
            1 FAMS @F1@
            0 @I2@ INDI
            1 _UID p-uid-02
            1 NAME Lucy /Johnson/
            1 SEX F
            1 FAMS @F1@
            0 @I3@ INDI
            1 _UID p-uid-03
            1 NAME Karol /Nowak/
            1 SEX M
            0 @F1@ FAM
            1 _UID fam-del-001
            1 HUSB @I1@
            1 WIFE @I2@
            1 MARR
            2 DATE 1 JAN 1960
            2 PLAC Gdańsk
            0 TRLR
            """;

        ByteArrayInputStream streamV2 = new ByteArrayInputStream(gedcomV2.getBytes(StandardCharsets.UTF_8));
        importService.importGedcom(streamV2);
        entityManager.flush();
        entityManager.clear();

        // THEN - only 1 family in the database, F2 removed
        List<Family> remainingFamilies = familyRepository.findAll();
        assertThat(remainingFamilies).hasSize(1);
        assertThat(familyRepository.findByGedcomDataGedUid("fam-del-002")).isEmpty();

        // F1 still exists unchanged
        Family fam1After = familyRepository.findByGedcomDataGedUid("fam-del-001").orElseThrow();
        assertThat(fam1After.getId()).isEqualTo(fam1Id);
        assertThat(fam1After.getPrimaryWedding().getId()).isEqualTo(weddingEventId);

        // All 3 people still exist (deleting a family does not delete people)
        assertThat(personRepository.findAll()).hasSize(3);
        assertThat(personRepository.findByGedcomDataGedUid("p-uid-03")).isPresent();

        // Wedding events of deleted F2 should be deleted in cascade
        // (F2 did not have a wedding with a date – no event – nothing to check)
        // It is verified that the wedding event F1 has NOT been deleted
        assertThat(eventRepository.findById(weddingEventId)).isPresent();
    }


    // Idempotentność importu
    @Test
    @Transactional
    void shouldProduceIdenticalStateWhenImportedTwice() throws Exception {
        // GIVEN
        String gedcomContent = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID idm-uid-001
            1 NAME Jan /Kowalski/
            1 SEX M
            1 BIRT
            2 DATE 10 JAN 1920
            2 PLAC Warszawa
            1 DEAT
            2 DATE 15 FEB 1990
            2 PLAC Kraków
            2 CAUS Zawał serca
            1 BURI
            2 PLAC Cmentarz Rakowicki, Kraków
            0 @I2@ INDI
            1 _UID idm-uid-002
            1 NAME Anna /Kowalska/
            1 SEX F
            1 FAMS @F1@
            0 @F1@ FAM
            1 _UID idm-fam-001
            1 HUSB @I1@
            1 WIFE @I2@
            1 CHIL @I1@
            1 MARR
            2 DATE 5 MAR 1950
            2 PLAC Gdańsk
            0 TRLR
            """;

        // WHEN - dwa identyczne importy
        importService.importGedcom(
                new ByteArrayInputStream(gedcomContent.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        importService.importGedcom(
                new ByteArrayInputStream(gedcomContent.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        // THEN - liczba rekordów taka sama jak po jednym imporcie
        assertThat(personRepository.findAll()).hasSize(2);
        assertThat(familyRepository.findAll()).hasSize(1);
        assertThat(eventRepository.findAll()).hasSize(4); // birth, death, burial, wedding
        assertThat(burialRepository.findAll()).hasSize(1);
        assertThat(eventLocationRepository.findAll()).hasSize(4);
        assertThat(personNameRepository.findAll()).hasSize(2);

        // Dane są poprawne po podwójnym imporcie
        Person jan = personRepository.findByGedcomDataGedUid("idm-uid-001").orElseThrow();
        assertThat(jan.getCachedGivenName()).isEqualTo("Jan");
        assertThat(jan.getBirthEvent()).isNotNull();
        assertThat(jan.getBirthEvent().getMainLocation().getName()).isEqualTo("Warszawa");
        assertThat(jan.getDeathEvent()).isNotNull();
        assertThat(jan.getBirthEvent().getParticipants()).hasSize(1);
        assertThat(jan.getDeathEvent().getParticipants()).hasSize(1);

        Family fam = familyRepository.findByGedcomDataGedUid("idm-fam-001").orElseThrow();
        assertThat(fam.getPrimaryWedding()).isNotNull();
        assertThat(fam.getPrimaryWedding().getParticipants()).hasSize(2);

        // Lokalizacje nie są duplikowane
        assertThat(locationRepository.findAll())
                .extracting(Location::getName)
                .containsExactlyInAnyOrder("Warszawa", "Kraków", "Cmentarz Rakowicki, Kraków", "Gdańsk");
    }


    // Współdzielenie lokalizacji
    @Test
    @Transactional
    void shouldShareLocationRecordsAcrossMultipleEvents() throws Exception {
        // GIVEN - dwie osoby urodzone w tym samym miejscu + ślub też tam
        String gedcomContent = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID loc-uid-001
            1 NAME Jan /Kowalski/
            1 SEX M
            1 BIRT
            2 DATE 1 JAN 1900
            2 PLAC Warszawa
            1 DEAT
            2 DATE 1 JAN 1980
            2 PLAC Warszawa
            0 @I2@ INDI
            1 _UID loc-uid-002
            1 NAME Anna /Nowak/
            1 SEX F
            1 BIRT
            2 DATE 1 MAR 1905
            2 PLAC Warszawa
            0 @F1@ FAM
            1 _UID loc-fam-001
            1 HUSB @I1@
            1 WIFE @I2@
            1 MARR
            2 DATE 5 JUN 1925
            2 PLAC Warszawa
            0 TRLR
            """;

        // WHEN
        importService.importGedcom(
                new ByteArrayInputStream(gedcomContent.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        // THEN - pomimo że "Warszawa" pojawia się w 4 eventach, w tabeli locations
        // istnieje dokładnie jeden rekord
        List<Location> locations = locationRepository.findAll();
        assertThat(locations)
                .extracting(Location::getName)
                .containsExactly("Warszawa");
        assertThat(locations).hasSize(1);
        Long warsawId = locations.getFirst().getId();

        // Wszystkie 4 eventy wskazują na ten sam rekord Location
        List<EventLocation> eventLocations = eventLocationRepository.findAll();
        assertThat(eventLocations).hasSize(4);
        assertThat(eventLocations)
                .extracting(el -> el.getLocation().getId())
                .containsOnly(warsawId);

        // Wszystkie eventy mają mainLocation wskazujące na ten sam rekord
        Person jan = personRepository.findByGedcomDataGedUid("loc-uid-001").orElseThrow();
        Person anna = personRepository.findByGedcomDataGedUid("loc-uid-002").orElseThrow();
        Family fam = familyRepository.findByGedcomDataGedUid("loc-fam-001").orElseThrow();

        assertThat(jan.getBirthEvent().getMainLocation().getId()).isEqualTo(warsawId);
        assertThat(jan.getDeathEvent().getMainLocation().getId()).isEqualTo(warsawId);
        assertThat(anna.getBirthEvent().getMainLocation().getId()).isEqualTo(warsawId);
        assertThat(fam.getPrimaryWedding().getMainLocation().getId()).isEqualTo(warsawId);
    }


    // Zmiana typu relacji dziecka przy re-imporcie
    @Test
    @Transactional
    void shouldUpdateChildRelationshipTypeOnSecondImport() throws Exception {
        // GIVEN - Mark jest biologicznym dzieckiem
        String gedcomV1 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID rel-uid-001
            1 NAME John /Smith/
            1 SEX M
            1 FAMS @F1@
            0 @I2@ INDI
            1 _UID rel-uid-002
            1 NAME Mark /Smith/
            1 SEX M
            1 FAMC @F1@
            0 @F1@ FAM
            1 _UID rel-fam-001
            1 HUSB @I1@
            1 CHIL @I2@
            1 MARR
            0 TRLR
            """;

        importService.importGedcom(
                new ByteArrayInputStream(gedcomV1.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        Family famBefore = familyRepository.findByGedcomDataGedUid("rel-fam-001").orElseThrow();
        FamilyChild markBefore = famBefore.getChildren().iterator().next();
        assertThat(markBefore.getRelationshipType()).isEqualTo(FamilyChildRelationshipType.BIOLOGICAL);
        Long markChildRelId = markBefore.getId();

        // WHEN - Mark staje się adoptowany
        String gedcomV2 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID rel-uid-001
            1 NAME John /Smith/
            1 SEX M
            1 FAMS @F1@
            0 @I2@ INDI
            1 _UID rel-uid-002
            1 NAME Mark /Smith/
            1 SEX M
            1 FAMC @F1@
            2 PEDI Adopted
            0 @F1@ FAM
            1 _UID rel-fam-001
            1 HUSB @I1@
            1 CHIL @I2@
            1 MARR
            0 TRLR
            """;

        importService.importGedcom(
                new ByteArrayInputStream(gedcomV2.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        // THEN
        Family famAfter = familyRepository.findByGedcomDataGedUid("rel-fam-001").orElseThrow();
        assertThat(famAfter.getChildren()).hasSize(1);

        FamilyChild markAfter = famAfter.getChildren().iterator().next();

        // Relacja zaktualizowana na ADOPTED
        assertThat(markAfter.getRelationshipType()).isEqualTo(FamilyChildRelationshipType.ADOPTED);

        // Raw data również zaktualizowane
        FamilyChildGedcomData rawChild = famAfter.getGedcomData().getChildren().iterator().next();
        assertThat(rawChild.getRawChildRefId()).isEqualTo("@I2@");
        assertThat(rawChild.getRawRelationshipType()).isEqualToIgnoringCase("Adopted");

        // Nie powstał nowy rekord FamilyChild – ten sam ID
        assertThat(markAfter.getId()).isEqualTo(markChildRelId);
    }


    // Usunięcie eventu birth przy re-imporcie
    @Test
    @Transactional
    void shouldDeleteBirthEventWhenRemovedFromSecondImport() throws Exception {
        // GIVEN - osoba z narodzinami i śmiercią
        String gedcomV1 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID evt-uid-001
            1 NAME Jan /Kowalski/
            1 SEX M
            1 BIRT
            2 DATE 10 JAN 1920
            2 PLAC Warszawa
            1 DEAT
            2 DATE 15 FEB 1990
            2 PLAC Kraków
            0 TRLR
            """;

        importService.importGedcom(
                new ByteArrayInputStream(gedcomV1.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        Person janBefore = personRepository.findByGedcomDataGedUid("evt-uid-001").orElseThrow();
        assertThat(janBefore.getBirthEvent()).isNotNull();
        assertThat(janBefore.getDeathEvent()).isNotNull();
        Long birthEventId = janBefore.getBirthEvent().getId();
        Long deathEventId = janBefore.getDeathEvent().getId();
        assertThat(eventRepository.findAll()).hasSize(2);
        assertThat(eventLocationRepository.findAll()).hasSize(2);

        // WHEN - drugi import: brak BIRT, zostaje tylko DEAT
        String gedcomV2 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID evt-uid-001
            1 NAME Jan /Kowalski/
            1 SEX M
            1 DEAT
            2 DATE 15 FEB 1990
            2 PLAC Kraków
            0 TRLR
            """;

        importService.importGedcom(
                new ByteArrayInputStream(gedcomV2.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        // THEN - event urodzin usunięty
        assertThat(eventRepository.findById(birthEventId)).isEmpty();
        assertThat(eventRepository.findById(deathEventId)).isPresent();
        assertThat(eventRepository.findAll()).hasSize(1);

        // EventLocation dla birth również usunięty
        assertThat(eventLocationRepository.findAll()).hasSize(1);
        assertThat(eventLocationRepository.findAll().getFirst().getLocation().getName())
                .isEqualTo("Kraków");

        // Osoba nadal istnieje, ale bez birthEvent
        Person janAfter = personRepository.findByGedcomDataGedUid("evt-uid-001").orElseThrow();
        assertThat(janAfter.getBirthEvent()).isNull();
        assertThat(janAfter.getDeathEvent()).isNotNull();
        assertThat(janAfter.getGedcomData().getRawBirthDate()).isNull();
        assertThat(janAfter.getGedcomData().getRawBirthPlace()).isNull();
    }


    // Usunięcie eventu wedding przy re-imporcie
    @Test
    @Transactional
    void shouldDeleteWeddingEventWhenRemovedFromSecondImport() throws Exception {
        // GIVEN - rodzina ze ślubem
        String gedcomV1 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID wevt-uid-001
            1 NAME John /Smith/
            1 SEX M
            1 FAMS @F1@
            0 @I2@ INDI
            1 _UID wevt-uid-002
            1 NAME Lucy /Johnson/
            1 SEX F
            1 FAMS @F1@
            0 @F1@ FAM
            1 _UID wevt-fam-001
            1 HUSB @I1@
            1 WIFE @I2@
            1 MARR
            2 DATE 5 JUN 1950
            2 PLAC Warszawa
            0 TRLR
            """;

        importService.importGedcom(
                new ByteArrayInputStream(gedcomV1.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        Family famBefore = familyRepository.findByGedcomDataGedUid("wevt-fam-001").orElseThrow();
        assertThat(famBefore.getPrimaryWedding()).isNotNull();
        Long weddingEventId = famBefore.getPrimaryWedding().getId();
        assertThat(eventRepository.findAll()).hasSize(1);
        assertThat(eventLocationRepository.findAll()).hasSize(1);

        // WHEN - drugi import: tag MARR bez daty i miejsca (brak eventu do zapisania)
        String gedcomV2 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID wevt-uid-001
            1 NAME John /Smith/
            1 SEX M
            1 FAMS @F1@
            0 @I2@ INDI
            1 _UID wevt-uid-002
            1 NAME Lucy /Johnson/
            1 SEX F
            1 FAMS @F1@
            0 @F1@ FAM
            1 _UID wevt-fam-001
            1 HUSB @I1@
            1 WIFE @I2@
            0 TRLR
            """;

        importService.importGedcom(
                new ByteArrayInputStream(gedcomV2.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        // THEN - event ślubu usunięty
        assertThat(eventRepository.findById(weddingEventId)).isEmpty();
        assertThat(eventRepository.findAll()).isEmpty();
        assertThat(eventLocationRepository.findAll()).isEmpty();

        // Rodzina nadal istnieje, ale bez primaryWedding
        Family famAfter = familyRepository.findByGedcomDataGedUid("wevt-fam-001").orElseThrow();
        assertThat(famAfter.getPrimaryWedding()).isNull();
        assertThat(famAfter.getGedcomData().getRawWeddingDate()).isNull();
        assertThat(famAfter.getGedcomData().getRawWeddingPlace()).isNull();
        // Status zmieniony na UNKNOWN (brak tagu MARR)
        assertThat(famAfter.getStatus().getCode()).isEqualTo(FamilyStatus.UNKNOWN.name());
    }


    // Usunięcie osoby będącej częścią rodziny
    @Test
    @Transactional
    void shouldNullifyFamilySpouseWhenPersonDeletedInSecondImport() throws Exception {
        // GIVEN - rodzina z mężem i żoną
        String gedcomV1 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID del-uid-001
            1 NAME John /Smith/
            1 SEX M
            1 FAMS @F1@
            0 @I2@ INDI
            1 _UID del-uid-002
            1 NAME Lucy /Johnson/
            1 SEX F
            1 FAMS @F1@
            0 @I3@ INDI
            1 _UID del-uid-003
            1 NAME Mark /Smith/
            1 SEX M
            1 FAMC @F1@
            0 @F1@ FAM
            1 _UID del-fam-001
            1 HUSB @I1@
            1 WIFE @I2@
            1 CHIL @I3@
            1 MARR
            2 DATE 1 JAN 1950
            2 PLAC Warszawa
            0 TRLR
            """;

        importService.importGedcom(
                new ByteArrayInputStream(gedcomV1.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        assertThat(personRepository.findAll()).hasSize(3);
        assertThat(familyRepository.findAll()).hasSize(1);

        // WHEN - drugi import: Lucy i Mark zniknęli, John i rodzina zostają
        String gedcomV2 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID del-uid-001
            1 NAME John /Smith/
            1 SEX M
            1 FAMS @F1@
            0 @F1@ FAM
            1 _UID del-fam-001
            1 HUSB @I1@
            1 MARR
            2 DATE 1 JAN 1950
            2 PLAC Warszawa
            0 TRLR
            """;

        importService.importGedcom(
                new ByteArrayInputStream(gedcomV2.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        // THEN - Lucy i Mark usunięci z bazy
        assertThat(personRepository.findAll()).hasSize(1);
        assertThat(personRepository.findByGedcomDataGedUid("del-uid-002")).isEmpty();
        assertThat(personRepository.findByGedcomDataGedUid("del-uid-003")).isEmpty();

        // Rodzina nadal istnieje (John jest w pliku)
        assertThat(familyRepository.findAll()).hasSize(1);
        Family famAfter = familyRepository.findByGedcomDataGedUid("del-fam-001").orElseThrow();

        // John nadal jest mężem
        assertThat(famAfter.getHusband()).isNotNull();
        assertThat(famAfter.getHusband().getCachedGivenName()).isEqualTo("John");

        // Lucy (żona) usunięta – pole wife jest null
        assertThat(famAfter.getWife()).isNull();

        // Mark (dziecko) usunięty – lista children pusta
        assertThat(famAfter.getChildren()).isEmpty();

        // Ślub nadal istnieje (John nadal w rodzinie)
        assertThat(famAfter.getPrimaryWedding()).isNotNull();
        assertThat(famAfter.getPrimaryWedding().getMainLocation().getName()).isEqualTo("Warszawa");

        // Uczestnicy ślubu: Lucy usunięta z event_participants
        assertThat(famAfter.getPrimaryWedding().getParticipants()).hasSize(1);
        assertThat(famAfter.getPrimaryWedding().getParticipants().iterator().next()
                .getPerson().getCachedGivenName()).isEqualTo("John");
    }


    // Import osoby bez eventów
    @Test
    @Transactional
    void shouldImportPersonWithNoEventsWithoutErrors() throws Exception {
        // GIVEN - osoba tylko z imieniem i płcią, bez żadnych eventów
        String gedcomContent = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID min-uid-001
            1 NAME Anna /Kowalska/
            1 SEX F
            0 TRLR
            """;

        // WHEN
        importService.importGedcom(
                new ByteArrayInputStream(gedcomContent.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        // THEN
        List<Person> persons = personRepository.findAll();
        assertThat(persons).hasSize(1);
        Person anna = persons.getFirst();

        assertThat(anna.getCachedGivenName()).isEqualTo("Anna");
        assertThat(anna.getCachedSurname()).isEqualTo("Kowalska");
        assertThat(anna.getSex()).isEqualTo(PersonSex.FEMALE);
        assertThat(anna.getLifeState()).isEqualTo(LifeState.UNKNOWN);
        assertThat(anna.getBirthEvent()).isNull();
        assertThat(anna.getDeathEvent()).isNull();
        assertThat(anna.getGedcomData().getRawBirthDate()).isNull();
        assertThat(anna.getGedcomData().getRawDeathDate()).isNull();

        assertThat(eventRepository.findAll()).isEmpty();
        assertThat(eventLocationRepository.findAll()).isEmpty();
        assertThat(burialRepository.findAll()).isEmpty();
    }


    // Rodzina z referencją do osoby nieistniejącej w pliku
    @Test
    @Transactional
    void shouldGracefullyHandleFamilyWithMissingPersonReference() throws Exception {
        // GIVEN - F1 odwołuje się do @I99@ którego nie ma w pliku
        String gedcomContent = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID missing-uid-001
            1 NAME John /Smith/
            1 SEX M
            1 FAMS @F1@
            0 @F1@ FAM
            1 _UID missing-fam-001
            1 HUSB @I1@
            1 WIFE @I99@
            1 CHIL @I98@
            1 MARR
            2 DATE 1 JAN 1950
            2 PLAC Warszawa
            0 TRLR
            """;

        // WHEN - import nie powinien rzucić wyjątku
        importService.importGedcom(
                new ByteArrayInputStream(gedcomContent.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        // THEN - import zakończony sukcesem, John i rodzina zostały zapisane
        assertThat(personRepository.findAll()).hasSize(1);
        assertThat(familyRepository.findAll()).hasSize(1);

        Family fam = familyRepository.findByGedcomDataGedUid("missing-fam-001").orElseThrow();
        assertThat(fam.getHusband()).isNotNull();
        assertThat(fam.getHusband().getCachedGivenName()).isEqualTo("John");

        // Nieistniejące referencje są ignorowane – wife i children puste
        assertThat(fam.getWife()).isNull();
        assertThat(fam.getChildren()).isEmpty();

        // Raw data zawiera oryginalne referencje z pliku
        assertThat(fam.getGedcomData().getRawWifeRefId()).isEqualTo("@I99@");
        assertThat(fam.getGedcomData().getChildren()).isEmpty();

        // Ślub zapisany mimo brakujących osób
        assertThat(fam.getPrimaryWedding()).isNotNull();
        assertThat(fam.getPrimaryWedding().getMainLocation().getName()).isEqualTo("Warszawa");
        // Uczestnik ślubu: tylko John (I99 nie istnieje)
        assertThat(fam.getPrimaryWedding().getParticipants()).hasSize(1);
    }


    // Sierota EventLocation – event traci miejsce przy re-imporcie
    @Test
    @Transactional
    void shouldDeleteEventLocationWhenPlaceRemovedOnSecondImport() throws Exception {
        // GIVEN
        String gedcomV1 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID evtloc-uid-001
            1 NAME Jan /Kowalski/
            1 SEX M
            1 BIRT
            2 DATE 10 JAN 1920
            2 PLAC Warszawa
            1 DEAT
            2 DATE 15 FEB 1990
            2 PLAC Kraków
            0 TRLR
            """;

        importService.importGedcom(
                new ByteArrayInputStream(gedcomV1.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        assertThat(eventLocationRepository.findAll()).hasSize(2);
        assertThat(locationRepository.findAll())
                .extracting(Location::getName)
                .containsExactlyInAnyOrder("Warszawa", "Kraków");

        Person janBefore = personRepository.findByGedcomDataGedUid("evtloc-uid-001").orElseThrow();
        Long birthEventId = janBefore.getBirthEvent().getId();

        // WHEN - drugi import: BIRT bez miejsca, DEAT bez miejsca
        String gedcomV2 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID evtloc-uid-001
            1 NAME Jan /Kowalski/
            1 SEX M
            1 BIRT
            2 DATE 10 JAN 1920
            1 DEAT
            2 DATE 15 FEB 1990
            0 TRLR
            """;

        importService.importGedcom(
                new ByteArrayInputStream(gedcomV2.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        // THEN – oba rekordy EventLocation usunięte
        assertThat(eventLocationRepository.findAll()).isEmpty();

        // Eventy nadal istnieją, tylko bez miejsca
        Person janAfter = personRepository.findByGedcomDataGedUid("evtloc-uid-001").orElseThrow();
        assertThat(janAfter.getBirthEvent()).isNotNull();
        assertThat(janAfter.getBirthEvent().getMainLocation()).isNull();
        assertThat(janAfter.getDeathEvent()).isNotNull();
        assertThat(janAfter.getDeathEvent().getMainLocation()).isNull();
    }


    // Sierota Grave – zmiana miejsca pochówku przy re-imporcie
    @Test
    @Transactional
    void shouldDeleteOrphanGraveWhenBurialPlaceChangedOnSecondImport() throws Exception {
        // GIVEN
        String gedcomV1 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID grave-uid-001
            1 NAME Jan /Kowalski/
            1 SEX M
            1 BURI
            2 PLAC Cmentarz Rakowicki, Kraków
            0 TRLR
            """;

        importService.importGedcom(
                new ByteArrayInputStream(gedcomV1.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        List<Burial> burialsAfterV1 = burialRepository.findAll();
        assertThat(burialsAfterV1).hasSize(1);
        Long oldGraveId = burialsAfterV1.getFirst().getGrave().getId();
        assertThat(burialsAfterV1.getFirst().getGrave().getCemeteryLocation().getName())
                .isEqualTo("Cmentarz Rakowicki, Kraków");

        // WHEN - nowe miejsce pochówku
        String gedcomV2 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID grave-uid-001
            1 NAME Jan /Kowalski/
            1 SEX M
            1 BURI
            2 PLAC Cmentarz Powązkowski, Warszawa
            0 TRLR
            """;

        importService.importGedcom(
                new ByteArrayInputStream(gedcomV2.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        // THEN – stary Grave usunięty, nowy na nowym miejscu
        assertThat(graveRepository.findAll()).hasSize(1);
        assertThat(graveRepository.findById(oldGraveId)).isEmpty();

        List<Burial> burialsAfterV2 = burialRepository.findAll();
        assertThat(burialsAfterV2).hasSize(1);
        assertThat(burialsAfterV2.getFirst().getGrave().getCemeteryLocation().getName())
                .isEqualTo("Cmentarz Powązkowski, Warszawa");

        // Stary Burial usunięty (tylko jeden rekord)
        assertThat(burialsAfterV2.getFirst().getGrave().getId()).isNotEqualTo(oldGraveId);
    }


    // Sierota Location – usunięty gdy nic na nią nie wskazuje
    @Test
    @Transactional
    void shouldDeleteLocationWhenNoLongerReferencedByAnyEvent() throws Exception {
        // GIVEN – Jan urodzony w Warszawie, Anna urodzona w Krakowie
        String gedcomV1 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID orphloc-uid-001
            1 NAME Jan /Kowalski/
            1 SEX M
            1 BIRT
            2 DATE 1 JAN 1900
            2 PLAC Warszawa
            0 @I2@ INDI
            1 _UID orphloc-uid-002
            1 NAME Anna /Nowak/
            1 SEX F
            1 BIRT
            2 DATE 1 JAN 1905
            2 PLAC Kraków
            0 TRLR
            """;

        importService.importGedcom(
                new ByteArrayInputStream(gedcomV1.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        assertThat(locationRepository.findAll())
                .extracting(Location::getName)
                .containsExactlyInAnyOrder("Warszawa", "Kraków");

        // WHEN - drugi import: Anna zniknęła, Kraków nie jest już nigdzie używany
        String gedcomV2 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID orphloc-uid-001
            1 NAME Jan /Kowalski/
            1 SEX M
            1 BIRT
            2 DATE 1 JAN 1900
            2 PLAC Warszawa
            0 TRLR
            """;

        importService.importGedcom(
                new ByteArrayInputStream(gedcomV2.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        // THEN – Kraków usunięty (nic już na niego nie wskazuje), Warszawa zostaje
        assertThat(locationRepository.findAll()).hasSize(1);
        assertThat(locationRepository.findAll().getFirst().getName()).isEqualTo("Warszawa");
        assertThat(locationRepository.findByName("Kraków")).isEmpty();

        // EventLocation dla Anny również usunięty
        assertThat(eventLocationRepository.findAll()).hasSize(1);
        assertThat(eventLocationRepository.findAll().getFirst().getLocation().getName())
                .isEqualTo("Warszawa");
    }


    // Sierota AttributeDictionaryValue – usunięty gdy nieużywany
    @Test
    @Transactional
    void shouldDeleteOrphanAttributeDictionaryValueWhenNoLongerUsed() throws Exception {
        // GIVEN – osoba z przyczyną śmierci
        String gedcomV1 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID attrval-uid-001
            1 NAME Jan /Kowalski/
            1 SEX M
            1 DEAT
            2 DATE 15 FEB 1990
            2 PLAC Kraków
            2 CAUS Zawał serca
            0 @I2@ INDI
            1 _UID attrval-uid-002
            1 NAME Anna /Nowak/
            1 SEX F
            1 DEAT
            2 DATE 1 JAN 2000
            2 PLAC Warszawa
            2 CAUS Wypadek
            0 TRLR
            """;

        importService.importGedcom(
                new ByteArrayInputStream(gedcomV1.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        assertThat(attrDictValueRepository.findAll())
                .extracting(AttributeDictionaryValue::getCode)
                .containsExactlyInAnyOrder("Zawał serca", "Wypadek");
        assertThat(eventAttrRepository.findAll()).hasSize(2);

        // WHEN - drugi import: Jan bez przyczyny śmierci, "Zawał serca" nie jest
        // już używane przez żaden event. "Wypadek" nadal używany przez Annę.
        String gedcomV2 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 _UID attrval-uid-001
            1 NAME Jan /Kowalski/
            1 SEX M
            1 DEAT
            2 DATE 15 FEB 1990
            2 PLAC Kraków
            0 @I2@ INDI
            1 _UID attrval-uid-002
            1 NAME Anna /Nowak/
            1 SEX F
            1 DEAT
            2 DATE 1 JAN 2000
            2 PLAC Warszawa
            2 CAUS Wypadek
            0 TRLR
            """;

        importService.importGedcom(
                new ByteArrayInputStream(gedcomV2.getBytes(StandardCharsets.UTF_8)));
        entityManager.flush();
        entityManager.clear();

        // THEN – "Zawał serca" usunięty (nikt go już nie używa)
        assertThat(attrDictValueRepository.findAll()).hasSize(1);
        assertThat(attrDictValueRepository.findAll().getFirst().getCode()).isEqualTo("Wypadek");

        // EventAttributeValue dla Jana usunięty
        assertThat(eventAttrRepository.findAll()).hasSize(1);

        // Weryfikacja po stronie eventów
        Person jan = personRepository.findByGedcomDataGedUid("attrval-uid-001").orElseThrow();
        Person anna = personRepository.findByGedcomDataGedUid("attrval-uid-002").orElseThrow();

        List<EventAttributeValue> janAttrs = eventAttrRepository.findAll().stream()
                .filter(eav -> eav.getEvent().getId().equals(jan.getDeathEvent().getId()))
                .toList();
        assertThat(janAttrs).isEmpty();
        assertThat(jan.getGedcomData().getRawCauseOfDeath()).isNull();

        List<EventAttributeValue> annaAttrs = eventAttrRepository.findAll().stream()
                .filter(eav -> eav.getEvent().getId().equals(anna.getDeathEvent().getId()))
                .toList();
        assertThat(annaAttrs).hasSize(1);
        assertThat(annaAttrs.getFirst().getAttributeDictionaryValue().getCode()).isEqualTo("Wypadek");
    }
}
