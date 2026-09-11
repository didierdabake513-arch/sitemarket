package com.esgis2026.assigame.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProduitDto {
    private Long id;
    private String nom;
    private Double prix;
    private Integer stock;
    private String categorie;
    private String emoji;
    private String description;
    private Double prixBarre;
    private Boolean enPromotion;
    private Boolean misEnAvant;
    /* Statut du produit : "ACTIF" ou "SUPPRIME" (suppression admin, avec
       possibilité de restauration). Uniquement utilisé par le Dashboard
       Admin — les autres pages ne reçoivent que les produits ACTIF. */
    private String statut;

    /* Vendeur qui a ajouté ce produit (affiché dans le Dashboard Admin) */
    private Long vendeurId;
    private String vendeurNom;

    /* Image "couverture" (data URL base64) — toujours égale à la première
       image de la galerie ci-dessous. Utilisée partout où une seule
       image suffit (cartes produit, panier, carrousel héros...). */
    private String imageBase64;

    /* Galerie complète du produit (jusqu'à ~10 photos, data URLs base64,
       dans l'ordre d'affichage). Utilisée sur la fiche produit détaillée. */
    private java.util.List<String> images;
}
