package com.musmike.familyheritage.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "family_child_gedcom_data")
@Getter
@Setter
@NoArgsConstructor
public class FamilyChildGedcomData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raw_relationship_type", length = 100)
    private String rawRelationshipType;

    @Column(name = "raw_child_ref_id", nullable = false, length = 100)
    private String rawChildRefId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "family_gedcom_id", nullable = false)
    private FamilyGedcomData familyGedcomData;
}