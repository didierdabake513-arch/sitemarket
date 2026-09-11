package com.esgis2026.assigame.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "produit")

public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_produit;

    @Column(unique = false, nullable = false, length = 50)
    private String nom_produit;

    @Column(unique = false, nullable = false, length = 1000)
    private String description_produit;

    @Column(unique = false, nullable = true)
    private double prix_produit;

    @Column(unique = false, nullable = true, length = 255)
    private String image_produit;

    @Column(unique = false, nullable = false)
    private LocalDateTime date_ajout;

    @Column(name = "status_produit", nullable = false)
    private String status_produit = "ACTIF";

    @Column(name = "statut", nullable = false)
    private String statut = "ACTIF";

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(length = 10)
    private String emoji;

    @Column(nullable = true)
    private Double prix_barre;

    @Column(nullable = false)
    private Boolean en_promotion = false;

    /* Mis en avant à la demande / décision de l'ADMIN uniquement (produit
       "conseillé" affiché en vitrine sur l'accueil). Un vendeur ne peut
       pas l'activer lui-même — seulement le demander (fonctionnalité de
       communication vendeur↔admin à venir). */
    @Column(name = "mis_en_avant", nullable = false, columnDefinition = "boolean default false")
    private Boolean mis_en_avant = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id")

    private CategorieProduit categorieProduit;

    /* Vendeur propriétaire du produit. Null = produit ajouté avant cette
       fonctionnalité, ou ajouté directement en base par un script initial. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendeur_id")
    private Utilisateur vendeur;

    /* Image du produit encodée en base64 (data URL complète,
       ex: "data:image/png;base64,iVBORw0KG...").
       Remplace l'ancien système d'emoji libre pour les produits
       ajoutés par un vendeur. Stocké en TEXT car une image en base64
       dépasse largement une colonne VARCHAR classique. */
    @Column(name = "image_base64", columnDefinition = "TEXT")
    private String imageBase64;

    /* Galerie complète (jusqu'à ~10 photos). imageBase64 ci-dessus reste
       à jour automatiquement avec la première image de cette liste — voir
       ProduitService — pour que tout le reste du site (qui ne connaît que
       cette "couverture") continue de fonctionner sans changement. */
    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("ordre ASC")
    private List<ProduitImage> images = new ArrayList<>();

    @PrePersist
    @PreUpdate
    void syncStatutColumns() {
        String value = status_produit != null ? status_produit : statut;
        if (value == null) {
            value = "ACTIF";
        }
        status_produit = value;
        statut = value;
    }
}
