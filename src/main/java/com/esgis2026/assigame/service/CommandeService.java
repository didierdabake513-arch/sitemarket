package com.esgis2026.assigame.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esgis2026.assigame.dto.CommandeDto;
import com.esgis2026.assigame.dto.CommandeItemDto;
import com.esgis2026.assigame.dto.CreateCommandeRequest;
import com.esgis2026.assigame.entity.Commande;
import com.esgis2026.assigame.entity.LigneCommande;
import com.esgis2026.assigame.entity.Produit;
import com.esgis2026.assigame.entity.StatutCommande;
import com.esgis2026.assigame.entity.Utilisateur;
import com.esgis2026.assigame.mapper.EntityMapper;
import com.esgis2026.assigame.repository.CommandeRepository;
import com.esgis2026.assigame.repository.GroupeLivraisonRepository;
import com.esgis2026.assigame.repository.ProduitRepository;
import com.esgis2026.assigame.repository.UtisateurRepository;
import com.esgis2026.assigame.util.SecurityUtils;

@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final UtisateurRepository utilisateurRepository;
    private final ProduitRepository produitRepository;
    private final GroupeLivraisonRepository groupeLivraisonRepository;

    public CommandeService(CommandeRepository commandeRepository,
                            UtisateurRepository utilisateurRepository,
                            ProduitRepository produitRepository,
                            GroupeLivraisonRepository groupeLivraisonRepository) {
        this.commandeRepository = commandeRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.produitRepository = produitRepository;
        this.groupeLivraisonRepository = groupeLivraisonRepository;
    }

    /** Toutes les commandes, tous vendeurs/clients confondus — réservé à l'administrateur. */
    @Transactional(readOnly = true)
    public List<CommandeDto> findAll() {
        if (!SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Réservé à l'administrateur");
        }
        return commandeRepository.findAll().stream()
                .map(EntityMapper::toCommandeDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommandeDto> findByUserId(Long userId) {
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return commandeRepository.findByUtilisateurOrderByDate_commandeDesc(user).stream()
                .map(c -> EntityMapper.toCommandeDto(c, true))
                .collect(Collectors.toList());
    }

    /**
     * Commandes contenant au moins un produit du VENDEUR actuellement
     * connecté. Filtrage fait ici, côté serveur — jamais confié au front.
     */
    @Transactional(readOnly = true)
    public List<CommandeDto> findByVendeurConnecte() {
        Long vendeurId = SecurityUtils.getCurrentUserId();
        return commandeRepository.findAll().stream()
                .filter(c -> c.getLignes().stream()
                        .anyMatch(l -> l.getProduit().getVendeur() != null
                                && l.getProduit().getVendeur().getId().equals(vendeurId)))
                .map(EntityMapper::toCommandeDto)
                .collect(Collectors.toList());
    }

    /**
     * Création d'une commande à partir du panier envoyé par le front.
     * Le total et le prix de chaque ligne sont systématiquement recalculés
     * ici à partir des prix réels en base — jamais à partir des valeurs
     * envoyées par le navigateur (qui pourraient être manipulées).
     *
     * Un CLIENT n'est jamais obligé de créer un compte pour commander :
     * si aucun token n'est envoyé (ou pas valide), la commande est créée
     * en mode "invité" (utilisateur = null), identifiée seulement par
     * l'adresse et le téléphone de livraison saisis dans le formulaire.
     */
    @Transactional
    public CommandeDto create(CreateCommandeRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Utilisateur client = userId != null ? utilisateurRepository.findById(userId).orElse(null) : null;

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Le panier est vide");
        }

        Commande commande = new Commande();
        commande.setReference(genererReference());
        commande.setCodeLivraison(genererCodeLivraison());
        commande.setUtilisateur(client);
        if (client == null) {
            String tel = request.getTelephone() != null ? request.getTelephone() : "N/A";
            commande.setNomClientInvite("Client invité (" + tel + ")");
        }
        commande.setStatut(StatutCommande.EN_ATTENTE);
        commande.setDate_commande(LocalDate.now());
        commande.setAdresseLivraison(request.getAdresseLivraison());
        commande.setTelephoneLivraison(request.getTelephone());
        commande.setModePaiement(request.getModePaiement());

        double total = 0;
        int nbArticles = 0;
        for (CommandeItemDto item : request.getItems()) {
            Produit produit = produitRepository.findById(item.getProduitId())
                    .orElseThrow(() -> new RuntimeException("Produit introuvable : " + item.getProduitId()));

            int quantite = item.getQuantite() != null ? item.getQuantite() : 1;
            if (produit.getStock() != null && produit.getStock() < quantite) {
                throw new IllegalArgumentException("Stock insuffisant pour " + produit.getNom_produit());
            }

            LigneCommande ligne = new LigneCommande();
            ligne.setCommande(commande);
            ligne.setProduit(produit);
            ligne.setQuantite(quantite);
            commande.getLignes().add(ligne);

            total += produit.getPrix_produit() * quantite;
            nbArticles += quantite;

            /* Décrémente le stock réel */
            produit.setStock(produit.getStock() - quantite);
            produitRepository.save(produit);
        }

        commande.setMontant_total(total);
        commande.setNb_articles(nbArticles);

        return EntityMapper.toCommandeDto(commandeRepository.save(commande), true);
    }

    /**
     * Changement de statut manuel (bouton côté Admin/Vendeur).
     * Si on passe à EXPEDIEE, on enregistre la date pour que la tâche
     * planifiée (CommandeAutoStatutScheduler) sache quand faire passer
     * automatiquement la commande à LIVREE.
     * Un VENDEUR ne peut changer le statut que d'une commande contenant
     * au moins un de ses propres produits.
     */
    @Transactional
    /**
     * Changement de statut d'une commande.
     *   - ADMIN   : peut tout changer.
     *   - VENDEUR : uniquement les commandes contenant l'un de ses produits.
     *   - CLIENT  : uniquement ANNULER sa propre commande, et seulement
     *               tant qu'elle est encore EN_ATTENTE (pas déjà expédiée).
     */
    public CommandeDto updateStatut(Long id, String statut) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        if (SecurityUtils.isVendeur()) {
            Long vendeurId = SecurityUtils.getCurrentUserId();
            boolean concerne = commande.getLignes().stream()
                    .anyMatch(l -> l.getProduit().getVendeur() != null
                            && l.getProduit().getVendeur().getId().equals(vendeurId));
            if (!concerne) {
                throw new AccessDeniedException("Cette commande ne contient aucun de vos produits");
            }
            if ("LIVREE".equals(statut)) {
                throw new IllegalStateException("Seul le scan du QR code par le livreur peut confirmer une livraison");
            }
        } else if (SecurityUtils.isClient()) {
            Long userId = SecurityUtils.getCurrentUserId();
            boolean estSaCommande = commande.getUtilisateur() != null && commande.getUtilisateur().getId().equals(userId);
            if (!estSaCommande) {
                throw new AccessDeniedException("Vous ne pouvez modifier que vos propres commandes");
            }
            if (!"ANNULEE".equals(statut)) {
                throw new AccessDeniedException("Vous ne pouvez qu'annuler votre commande");
            }
            if (commande.getStatut() != StatutCommande.EN_ATTENTE) {
                throw new IllegalStateException("Cette commande a déjà été prise en charge et ne peut plus être annulée");
            }
        }
        // ADMIN : aucune restriction.

        StatutCommande nouveauStatut = StatutCommande.valueOf(statut);
        commande.setStatut(nouveauStatut);
        if (nouveauStatut == StatutCommande.EXPEDIEE) {
            commande.setDateExpedition(LocalDate.now());
        }
        return EntityMapper.toCommandeDto(commandeRepository.save(commande));
    }

    private String genererReference() {
        return "#" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /* Code unique encodé dans le QR remis au client — volontairement long et
       aléatoire (UUID complet) pour ne pas pouvoir être deviné ou reconstruit
       à partir de la référence de commande, qui elle est publique. */
    private String genererCodeLivraison() {
        return UUID.randomUUID().toString();
    }

    /**
     * Le vendeur assigne un de ses livreurs à une commande contenant ses
     * produits. La commande passe alors à EXPEDIEE (le livreur part avec le
     * colis) — elle ne pourra passer à LIVREE que lorsque ce même livreur
     * scannera le QR code du client (voir confirmerLivraisonParCode ci-dessous).
     */
    /*@Transactional
    /**
     * Le vendeur met une commande "de côté" chez un de ses livreurs — ou
     * chez un livreur d'un groupe de livraison qu'il a rejoint. La commande
     * reste EN_ATTENTE (le client peut encore l'annuler) tant que le
     * livreur n'est pas réellement parti : voir fairePartirTournee()
     * ci-dessous, qui est le seul moment où le statut passe à EXPEDIEE et
     * où le tarif de livraison est calculé.
     */
    @Transactional
    public CommandeDto assignerLivreur(Long commandeId, Long livreurId) {
        if (!SecurityUtils.isVendeur() && !SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Réservé aux vendeurs et à l'administrateur");
        }
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));
        Long vendeurId = SecurityUtils.getCurrentUserId();
        if (SecurityUtils.isVendeur()) {
            boolean concerne = commande.getLignes().stream()
                    .anyMatch(l -> l.getProduit().getVendeur() != null
                            && l.getProduit().getVendeur().getId().equals(vendeurId));
            if (!concerne) {
                throw new AccessDeniedException("Cette commande ne contient aucun de vos produits");
            }
        }
        Utilisateur livreur = utilisateurRepository.findById(livreurId)
                .orElseThrow(() -> new RuntimeException("Livreur introuvable"));
        if (!"LIVREUR".equalsIgnoreCase(livreur.getTypeUtilisateur().getNom_utilisateur())) {
            throw new IllegalArgumentException("Cet utilisateur n'est pas un livreur");
        }
        if (SecurityUtils.isVendeur() && !livreurAccessible(livreur, vendeurId)) {
            throw new AccessDeniedException("Ce livreur n'est ni dans votre équipe, ni dans un groupe de livraison que vous avez rejoint");
        }
        commande.setLivreur(livreur);
        return EntityMapper.toCommandeDto(commandeRepository.save(commande));
    }

    /** Un livreur est accessible à un vendeur s'il lui appartient en propre,
     *  ou si le vendeur a rejoint un groupe créé par le propriétaire du livreur. */
    private boolean livreurAccessible(Utilisateur livreur, Long vendeurId) {
        if (livreur.getVendeurProprietaire() == null) return false;
        if (livreur.getVendeurProprietaire().getId().equals(vendeurId)) return true;
        return groupeLivraisonRepository.findByCreateur_Id(livreur.getVendeurProprietaire().getId()).stream()
                .anyMatch(g -> g.getMembres().stream().anyMatch(m -> m.getId().equals(vendeurId)));
    }

    /**
     * Le VENDEUR PROPRIÉTAIRE du livreur le fait partir avec tous les colis
     * actuellement mis de côté pour lui (commandes EN_ATTENTE qui lui sont
     * assignées, quel que soit le vendeur d'origine de chaque commande —
     * c'est tout l'intérêt du groupe). Le tarif de chaque commande est
     * calculé à cet instant précis, en fonction du nombre total de colis
     * dans cette tournée : plus il y en a, plus le prix unitaire baisse.
     */
    @Transactional
    public List<CommandeDto> faireePartirTournee(Long livreurId) {
        if (!SecurityUtils.isVendeur()) {
            throw new AccessDeniedException("Réservé aux vendeurs");
        }
        Utilisateur livreur = utilisateurRepository.findById(livreurId)
                .orElseThrow(() -> new RuntimeException("Livreur introuvable"));
        Long vendeurId = SecurityUtils.getCurrentUserId();
        if (livreur.getVendeurProprietaire() == null || !livreur.getVendeurProprietaire().getId().equals(vendeurId)) {
            throw new AccessDeniedException("Seul le vendeur propriétaire de ce livreur peut le faire partir");
        }
        List<Commande> tournee = commandeRepository.findByLivreur_IdAndStatut(livreurId, StatutCommande.EN_ATTENTE);
        if (tournee.isEmpty()) {
            throw new IllegalStateException("Aucun colis en attente pour ce livreur");
        }
        double tarifUnitaire = tarifLivraisonParPalier(tournee.size());
        LocalDate aujourdHui = LocalDate.now();
        for (Commande c : tournee) {
            c.setStatut(StatutCommande.EXPEDIEE);
            c.setDateExpedition(aujourdHui);
            c.setTarifLivraison(tarifUnitaire);
        }
        commandeRepository.saveAll(tournee);
        return tournee.stream().map(c -> EntityMapper.toCommandeDto(c, false)).collect(Collectors.toList());
    }

    /**
     * Paliers de tarification dégressive — plus le livreur emporte de colis
     * dans la même tournée, moins chaque colis coûte à livrer (il rentabilise
     * mieux son trajet). Valeurs simples et fixes pour l'instant, en
     * attendant un vrai calcul basé sur les distances réelles (nécessiterait
     * une API de cartographie externe — voir notes projet).
     */
    public static double tarifLivraisonParPalier(int nbColis) {
        if (nbColis >= 7) return 700;
        if (nbColis >= 4) return 900;
        if (nbColis >= 2) return 1200;
        return 1500;
    }

    /** Estimation grossière de délai avant livraison, basée uniquement sur
     *  combien de colis attendent déjà ce livreur (aucune API externe). */
    public static String estimationDelai(int nbColisEnAttente) {
        if (nbColisEnAttente <= 1) return "2 à 4 h après le départ";
        if (nbColisEnAttente <= 3) return "4 à 8 h après le départ";
        return "8 à 24 h après le départ";
    }

    /**
     * Le livreur scanne le QR code remis au client à la commande. Si le code
     * correspond à une commande qui LUI est bien assignée et pas déjà
     * livrée/annulée, elle passe à LIVREE — c'est le seul chemin possible
     * vers ce statut, garantissant que le vendeur n'est "payé" (voir
     * versement — fonctionnalité à venir) qu'après une livraison confirmée
     * physiquement, pas juste déclarée.
     */
    @Transactional
    public CommandeDto confirmerLivraisonParCode(String codeLivraison) {
        if (!SecurityUtils.isLivreur()) {
            throw new AccessDeniedException("Réservé aux livreurs");
        }
        Commande commande = commandeRepository.findByCodeLivraison(codeLivraison)
                .orElseThrow(() -> new IllegalArgumentException("Code de livraison invalide ou inconnu"));
        Long livreurId = SecurityUtils.getCurrentUserId();
        if (commande.getLivreur() == null || !commande.getLivreur().getId().equals(livreurId)) {
            throw new AccessDeniedException("Cette commande n'est pas assignée à ce livreur");
        }
        if (commande.getStatut() == StatutCommande.LIVREE) {
            throw new IllegalStateException("Cette commande a déjà été marquée comme livrée");
        }
        if (commande.getStatut() == StatutCommande.ANNULEE) {
            throw new IllegalStateException("Cette commande a été annulée");
        }
        commande.setStatut(StatutCommande.LIVREE);
        return EntityMapper.toCommandeDto(commandeRepository.save(commande));
    }

    /** Commandes actuellement assignées au livreur connecté, à livrer. */
    public List<CommandeDto> mesLivraisons() {
        if (!SecurityUtils.isLivreur()) {
            throw new AccessDeniedException("Réservé aux livreurs");
        }
        Long livreurId = SecurityUtils.getCurrentUserId();
        return commandeRepository.findByLivreur_Id(livreurId).stream()
                .filter(c -> c.getStatut() == StatutCommande.EXPEDIEE)
                .map(c -> EntityMapper.toCommandeDto(c, true))
                .collect(Collectors.toList());
    }
}
