package com.musmike.familyheritage.integration.service;

import com.musmike.familyheritage.integration.AbstractIntegrationTest;
import com.musmike.familyheritage.model.LifeState;
import com.musmike.familyheritage.model.Sex;
import com.musmike.familyheritage.repository.PersonRepository;
import com.musmike.familyheritage.service.GedcomImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.assertThat;

public class GedcomImportServiceTest extends AbstractIntegrationTest {
    @Autowired
    private GedcomImportService importService;

    @Autowired
    private PersonRepository personRepository;

    @Test
    void shouldImportNewPersonFromGedcomContent() throws Exception {
        // GIVEN
        String gedcomContent = """
            0 HEAD
            1 SOUR MyHeritage
            1 CHAR UTF-8
            0 @I1@ INDI
            1 NAME Jan /Kowalski/
            1 SEX M
            0 TRLR
            """;

        ByteArrayInputStream inputStream = new ByteArrayInputStream(gedcomContent.getBytes(StandardCharsets.UTF_8));

        // WHEN
        importService.importGedcom(inputStream);

        // THEN
        var persons = personRepository.findAll();
        assertThat(persons).hasSize(1);

        var person = persons.getFirst();
        assertThat(person.getCachedGivenName()).isEqualTo("Jan");
        assertThat(person.getCachedSurname()).isEqualTo("Kowalski");
        assertThat(person.getSex()).isEqualTo(Sex.MALE);
        assertThat(person.getLifeState()).isEqualTo(LifeState.UNKNOWN);

        assertThat(person.getGedcomData()).isNotNull();
        assertThat(person.getGedcomData().getGedRefId()).isEqualTo("@I1@");
        assertThat(person.getGedcomData().getRawGivenName()).isEqualTo("Jan");
        assertThat(person.getGedcomData().getRawSurname()).isEqualTo("Kowalski");
        assertThat(person.getGedcomData().getRawSex()).isEqualTo("M");
        assertThat(person.getGedcomData().isRawDeathIndicated()).isEqualTo(false);
    }

    @Test
    void shouldUpdateExistingPersonWhenImportedAgainWithChanges() throws Exception {
        // GIVEN
        String gedcomV1 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 NAME Jan /Kowalski/
            1 SEX M
            0 TRLR
            """;

        ByteArrayInputStream inputStreamV1 = new ByteArrayInputStream(gedcomV1.getBytes(StandardCharsets.UTF_8));
        importService.importGedcom(inputStreamV1);

        assertThat(personRepository.findAll()).hasSize(1);
        assertThat(personRepository.findAll().getFirst().getCachedGivenName()).isEqualTo("Jan");

        // WHEN
        String gedcomV2 = """
            0 HEAD
            1 SOUR Test
            1 CHAR UTF-8
            0 @I1@ INDI
            1 NAME Janusz /Kowalski/
            1 SEX M
            0 TRLR
            """;

        ByteArrayInputStream inputStreamV2 = new ByteArrayInputStream(gedcomV2.getBytes(StandardCharsets.UTF_8));
        importService.importGedcom(inputStreamV2);

        // THEN
        var persons = personRepository.findAll();
        assertThat(persons).hasSize(1);

        var person = persons.getFirst();
        assertThat(person.getCachedGivenName()).isEqualTo("Janusz");
        assertThat(person.getGedcomData().getGedRefId()).isEqualTo("@I1@");
    }
}
