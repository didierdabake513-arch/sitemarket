package com.esgis2026.assigame.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * Groupe de livraison : un vendeur qui a son propre service de livraison
 * (au moins un livreur) peut créer un groupe. D'autres vendeurs peuvent le
 * rejoindre librement (aucune validation admin) pour pouvoir eux aussi
 * assigner leurs commandes aux livreurs de ce groupe.
 */
@Entity
@Getter
@Setter
@Table(name = "groupe_livraison")
public class GroupeLivraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    /* Le vendeur créateur — c'est SON équipe de livreurs qui est partagée
       avec les membres du groupe. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_createur", nullable = false)
    private Utilisateur createur;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(name = "groupe_livraison_membre",
            joinColumns = @JoinColumn(name = "id_groupe"),
            inverseJoinColumns = @JoinColumn(name = "id_vendeur"))
    private Set<Utilisateur> membres = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();
}
