package com.esgis2026.assigame.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "commande")
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String reference;

    /* Nullable : un client peut commander sans créer de compte (commande
       "invité"). S'il est connecté, l'utilisateur est renseigné normalement
       et il retrouve sa commande dans /api/commandes/me. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur", nullable = true)
    private Utilisateur utilisateur;

    /* Renseigné uniquement pour une commande "invité" (utilisateur == null),
       à partir du formulaire de commande — sert à afficher un nom côté
       Vendeur/Admin puisqu'il n'y a pas de compte à consulter. */
    @Column(name = "nom_client_invite", length = 150)
    private String nomClientInvite;

    @Column(nullable = false)
    private Double montant_total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutCommande statut = StatutCommande.EN_ATTENTE;

    @Column(nullable = false)
    private LocalDate date_commande;

    @Column(nullable = false)
    private Integer nb_articles;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommande> lignes = new ArrayList<>();

    /* Renseignés au moment de la validation de la commande (page Commande.html) */
    @Column(name = "adresse_livraison", length = 255)
    private String adresseLivraison;

    @Column(name = "telephone_livraison", length = 20)
    private String telephoneLivraison;

    /* MOBILE_MONEY · CARTE · LIVRAISON — pour l'instant seul LIVRAISON est
       réellement utilisable (les deux autres sont bloqués côté front car ils
       nécessitent une passerelle de paiement externe non encore intégrée). */
    @Column(name = "mode_paiement", length = 20)
    private String modePaiement;

    /* Renseignée automatiquement quand le statut passe à EXPEDIEE. */
    @Column(name = "date_expedition")
    private LocalDate dateExpedition;

    /* Livreur assigné à cette commande par le vendeur (nullable tant que
       personne n'est encore assigné). Tant que le statut reste EN_ATTENTE
       avec un livreur déjà renseigné, la commande est "en attente de
       départ" : mise de côté chez le livreur, qui peut accumuler plusieurs
       colis avant de partir (voir LivreurService.faireePartirTournee). Le
       statut ne passe à EXPEDIEE qu'au moment du départ réel. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_livreur", nullable = true)
    private Utilisateur livreur;

    /* Tarif de livraison — calculé uniquement au moment où le livreur part
       réellement (jamais avant), en fonction du nombre de colis qu'il
       emporte dans cette même tournée : plus il y en a, plus le prix
       unitaire baisse. Nul tant que la commande n'a pas encore été
       expédiée. */
    @Column(name = "tarif_livraison")
    private Double tarifLivraison;

    /* Code unique remis au client à la commande (imprimé sous forme de QR
       code) — c'est en scannant CE code que le livreur confirme la
       livraison et fait passer la commande à LIVREE (voir
       LivraisonController). Généré une fois pour toutes à la création. */
    @Column(name = "code_livraison", unique = true, length = 40)
    private String codeLivraison;
}
