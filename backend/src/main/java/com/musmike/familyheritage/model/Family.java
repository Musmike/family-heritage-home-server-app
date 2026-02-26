package com.musmike.familyheritage.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "families")
@Getter
@Setter
@NoArgsConstructor
public class Family {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "husband_id")
    private Person husband;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wife_id")
    private Person wife;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "primary_wedding_id")
    private Event primaryWedding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private UserCode status;

    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FamilyChild> children = new HashSet<>();

    @OneToOne(mappedBy = "family", cascade = CascadeType.ALL, orphanRemoval = true)
    private FamilyGedcomData gedcomData;

    public void addChild(FamilyChild child) {
        children.add(child);
        child.setFamily(this);
    }
}