package com.esgis2026.assigame.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LivreurDto {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    /* Mot de passe : uniquement reçu à la création, jamais renvoyé. */
    private String password;
    private Boolean bloque;
    /** Nombre de livraisons actuellement en cours (statut EXPEDIEE) — utile
     *  pour le vendeur avant d'assigner une nouvelle commande. */
    private Integer livraisonsEnCours;
    /** Colis mis de côté pour ce livreur mais pas encore partis (statut
     *  EN_ATTENTE avec ce livreur assigné) — c'est sa "tournée" en préparation. */
    private Integer colisEnAttenteDepart;
    /** true si ce livreur appartient à un AUTRE vendeur (via un groupe de
     *  livraison rejoint) plutôt qu'au vendeur qui consulte la liste. */
    private Boolean livreurDeGroupe;
    private String proprietaireNom;
    private String estimationDelai;
}
