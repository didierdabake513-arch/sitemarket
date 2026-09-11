package com.esgis2026.assigame.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Bannière promotionnelle de l'accueil ("Jusqu'à -40% ce weekend !").
 * Une seule ligne en base (id = 1) — modifiable uniquement par l'admin
 * depuis le Dashboard, affichée telle quelle sur l'accueil.
 */
@Entity
@Getter
@Setter
@Table(name = "promo_banner")
public class PromoBanner {

    @Id
    private Long id = 1L;

    @Column(nullable = false, length = 150)
    private String titre = "Jusqu'à -40% ce weekend !";

    @Column(nullable = false, length = 200)
    private String sousTitre = "Offres limitées sur les chaussures et l'électronique";

    @Column(nullable = false, length = 60)
    private String texteBouton = "Profiter des offres";

    @Column(nullable = false, length = 200)
    private String lienBouton = "../catalogue/Catalogue.html";

    @Column(nullable = false)
    private Boolean actif = true;

    /* Texte affiché en surimpression sur le carrousel des produits mis en
       avant (accueil). Modifiable par l'admin depuis Paramètres du site.
       columnDefinition avec valeur par défaut : évite l'échec de migration
       déjà rencontré une fois avec une colonne NOT NULL sans défaut sur
       une table déjà peuplée. */
    @Column(name = "mis_en_avant_titre", nullable = false, length = 150,
            columnDefinition = "varchar(150) default 'Notre sélection du moment'")
    private String misEnAvantTitre = "Notre sélection du moment";

    @Column(name = "mis_en_avant_texte", nullable = false, length = 300,
            columnDefinition = "varchar(300) default ''")
    private String misEnAvantTexte = "";
}
