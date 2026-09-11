package com.esgis2026.assigame.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommandeItemDto {
    private Long produitId;
    private Integer quantite;
    private String produitNom;
    private String vendeurNom;

    public CommandeItemDto(Long produitId, Integer quantite) {
        this.produitId = produitId;
        this.quantite = quantite;
    }
}
