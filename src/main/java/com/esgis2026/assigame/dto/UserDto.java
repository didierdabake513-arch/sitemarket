package com.esgis2026.assigame.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String role;
    private Boolean bloque;
    private String dateNaissance;
    private String adresse;
    private String region;
    /* Uniquement pour un compte LIVREUR : le vendeur qui l'a créé, pour
       qu'il puisse le contacter facilement depuis son espace. */
    private Long vendeurProprietaireId;
    private String vendeurProprietaireNom;
    /* Solde simulé (vérification "peut payer" avant une enchère). */
    private Double solde;
}
