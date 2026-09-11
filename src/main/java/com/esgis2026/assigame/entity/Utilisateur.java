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

@Entity
@Getter
@Setter
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Long id;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false, length = 50)
    private String prenom;

    @Column(unique = true, nullable = false,length = 100)
    private String email;

    @Column(nullable = false,length = 100)
    private String motdepasse;

    @Column (nullable = true,length = 20)
    private String telephone;

    @Column(nullable = false)
    private LocalDateTime date_creation;

    @Column(nullable = false, length = 20)
    private String statut;

    @Column(length = 20)
    private String date_naissance;

    @Column(length = 255)
    private String adresse;

    /* Région du Togo (Maritime, Plateaux, Centrale, Kara, Savanes) — permet
       de mieux organiser/filtrer les utilisateurs (voir Dashboard Admin). */
    @Column(length = 30)
    private String region;

    @Column(columnDefinition = "TEXT")
    private String notifications_json;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_didierutilisateur", nullable = false)
    private TypeUtilisateur typeUtilisateur;

    /* Uniquement pour un compte LIVREUR : le vendeur qui l'a créé et pour
       qui il travaille. Non utilisé pour les autres rôles. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vendeur_proprietaire", nullable = true)
    private Utilisateur vendeurProprietaire;

    /* Solde utilisé UNIQUEMENT pour la vérification "peut payer" avant de
       participer à une enchère (voir EnchereService). Aucun vrai débit ni
       versement : l'argent ne quitte jamais réellement ce compte — c'est
       une simulation en attendant un vrai prestataire de paiement.
       Réglable par l'admin depuis le Dashboard. */
    @Column(nullable = false, columnDefinition = "double precision default 0")
    private Double solde = 0.0;

}
