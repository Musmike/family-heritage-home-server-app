package com.musmike.familyheritage.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "people")
@Data
@NoArgsConstructor
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cached_given_name")
    private String cachedGivenName;

    @Column(name = "cached_surname")
    private String cachedSurname;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Sex sex;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private LifeState lifeState = LifeState.UNKNOWN;

    @OneToOne(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private PersonGedcomData gedcomData;
}
