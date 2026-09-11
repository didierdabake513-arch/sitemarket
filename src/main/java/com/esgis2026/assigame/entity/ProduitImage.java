package com.esgis2026.assigame.entity;

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
 * Une photo de la galerie d'un produit (un produit peut en avoir plusieurs —
 * jusqu'à ~10 en pratique côté front). "ordre" fixe leur position d'affichage.
 * La toute première (ordre = 0) est aussi recopiée dans Produit.imageBase64
 * pour rester la "couverture" utilisée partout ailleurs sur le site
 * (cartes produit, carrousel héros, panier...) sans avoir à toucher ce code.
 */
@Entity
@Getter
@Setter
@Table(name = "produit_image")
public class ProduitImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(name = "image_base64", columnDefinition = "TEXT", nullable = false)
    private String imageBase64;

    @Column(nullable = false)
    private Integer ordre = 0;
}
