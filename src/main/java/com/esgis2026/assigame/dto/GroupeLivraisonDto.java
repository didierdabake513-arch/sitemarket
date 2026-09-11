package com.esgis2026.assigame.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupeLivraisonDto {
    private Long id;
    private String nom;
    private Long createurId;
    private String createurNom;
    /* true si l'utilisateur courant a créé ce groupe ou l'a rejoint —
       pratique pour l'affichage front (bouton "Rejoindre" vs "Membre"). */
    private Boolean estMembre;
    private Integer nbMembres;
    private Integer nbLivreurs;
    private List<String> membresNoms;
}
