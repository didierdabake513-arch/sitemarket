package com.esgis2026.assigame.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Corps attendu pour POST /api/commandes.
 * Le montantTotal envoyé par le front est ignoré : il est toujours
 * recalculé côté serveur à partir des prix réels en base (voir
 * CommandeService.create) pour éviter qu'un client modifie le prix
 * depuis le navigateur.
 */
@Getter
@Setter
public class CreateCommandeRequest {
    private String adresseLivraison;
    private String telephone;
    private String modePaiement;
    private List<CommandeItemDto> items;
}
