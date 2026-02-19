package com.musmike.familyheritage.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "person_gedcom_data")
@Data
@NoArgsConstructor
public class PersonGedcomData {
    @Id
    private Long personId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "person_id")
    private Person person;

    @Column(name = "ged_ref_id", nullable = false, length = 100)
    private String gedRefId;

    private String rawGivenName;
    private String rawSurname;

    @Column(length = 1)
    private String rawSex;

    @Column(name = "raw_death_indicated")
    private boolean rawDeathIndicated = false;
}
