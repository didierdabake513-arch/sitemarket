package com.esgis2026.assigame.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromoBannerDto {
    private String titre;
    private String sousTitre;
    private String texteBouton;
    private String lienBouton;
    private Boolean actif;
    private String misEnAvantTitre;
    private String misEnAvantTexte;
}
