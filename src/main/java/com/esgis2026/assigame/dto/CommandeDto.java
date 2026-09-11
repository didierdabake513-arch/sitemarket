package com.esgis2026.assigame.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommandeDto {
    private Long id;
    private String reference;
    private String clientNom;
    private Long clientId;
    private String clientEmail;
    private Double montantTotal;
    private String statut;
    private String dateCommande;
    private Integer nbArticles;
    private String emoji;
    private List<CommandeItemDto> items;
    /* Nom(s) du/des vendeur(s) concerné(s) par cette commande — pour que
       l'admin voie d'un coup d'œil à quel vendeur elle correspond, sans
       devoir dérouler chaque article (une commande peut mélanger les
       produits de plusieurs vendeurs). */
    private String vendeursNoms;

    /* Renseignés à la création (POST /api/commandes) */
    private String adresseLivraison;
    private String telephone;
    private String modePaiement;

    /* Livraison / QR code — codeLivraison n'est renvoyé que dans la réponse
       de création de commande (pour l'acheteur) et à l'admin/vendeur/livreur
       concernés (voir EntityMapper) : jamais dans une liste générale. */
    private String codeLivraison;
    private Long livreurId;
    private String livreurNom;
    private Double tarifLivraison;
    private String dateExpedition;
}
