package com.esgis2026.assigame.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * Enchère sur un produit — fonctionnalité en test, réservée à l'admin pour
 * le moment (indisponible au public — voir EnchereService). Le statut n'est
 * jamais stocké : il est recalculé à la volée depuis dateDebut/dateFin, pour
 * ne jamais désynchroniser (voir EnchereService.calculerStatut).
 */
@Entity
@Getter
@Setter
@Table(name = "enchere")
public class Enchere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    /* Prix de départ — volontairement inférieur au prix normal du produit. */
    @Column(nullable = false)
    private Double prixDepart;

    /* Prix actuel = le prix de départ tant qu'aucune offre, sinon la
       meilleure offre reçue. */
    @Column(nullable = false)
    private Double prixActuel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_meilleur_encherisseur", nullable = true)
    private Utilisateur meilleurEncherisseur;

    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @Column(nullable = false)
    private LocalDateTime dateFin;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();
}
