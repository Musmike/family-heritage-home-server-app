package com.musmike.familyheritage.model;

import com.musmike.familyheritage.model.enums.LifeState;
import com.musmike.familyheritage.model.enums.PersonSex;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "people")
@Getter
@Setter
@NoArgsConstructor
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cachedGivenName;
    private String cachedSurname;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PersonSex sex;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private LifeState lifeState = LifeState.UNKNOWN;

    @OneToOne(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private PersonGedcomData gedcomData;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "birth_event_id")
    private Event birthEvent;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "death_event_id")
    private Event deathEvent;
}