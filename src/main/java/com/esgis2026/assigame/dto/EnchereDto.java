package com.esgis2026.assigame.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnchereDto {
    private Long id;
    private Long produitId;
    private String produitNom;
    private String produitImage;
    private Double prixNormal;
    private Double prixDepart;
    private Double prixActuel;
    private String meilleurEncherisseurNom;
    private String dateDebut;
    private String dateFin;
    /** A_VENIR (aperçu), EN_COURS (on peut enchérir), TERMINEE (clôturée). */
    private String statut;
    private Integer nbOffres;
}
