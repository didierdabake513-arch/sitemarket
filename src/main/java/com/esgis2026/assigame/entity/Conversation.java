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
 * Conversation privée et confidentielle entre deux comptes — jamais visible
 * par un tiers depuis l'application (ni un autre client, ni un autre
 * vendeur). Ce n'est PAS un chiffrement de bout en bout (le serveur peut
 * techniquement lire le contenu en base) : voir la note dans MessageService.
 *
 * Trois types autorisés seulement :
 *   ACHETEUR_VENDEUR — un client contacte un vendeur à propos d'un produit
 *   VENDEUR_VENDEUR  — deux vendeurs échangent (ex. dans un groupe de livraison)
 *   VENDEUR_LIVREUR  — un vendeur et un de ses livreurs (ou d'un groupe rejoint)
 */
@Entity
@Getter
@Setter
@Table(name = "conversation")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_participant1", nullable = false)
    private Utilisateur participant1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_participant2", nullable = false)
    private Utilisateur participant2;

    /* Contexte optionnel : le produit à propos duquel un client a contacté
       un vendeur (permet d'afficher "à propos de <produit>" côté front). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit_contexte", nullable = true)
    private Produit produitContexte;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();
}
