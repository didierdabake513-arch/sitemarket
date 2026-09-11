package com.esgis2026.assigame.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esgis2026.assigame.dto.EnchereDto;
import com.esgis2026.assigame.entity.Enchere;
import com.esgis2026.assigame.entity.EnchereOffre;
import com.esgis2026.assigame.entity.Produit;
import com.esgis2026.assigame.entity.Utilisateur;
import com.esgis2026.assigame.repository.EnchereOffreRepository;
import com.esgis2026.assigame.repository.EnchereRepository;
import com.esgis2026.assigame.repository.ProduitRepository;
import com.esgis2026.assigame.repository.UtisateurRepository;
import com.esgis2026.assigame.util.SecurityUtils;

/**
 * Enchères hebdomadaires — FONCTIONNALITÉ EN TEST, réservée à l'admin pour
 * le moment (comme convenu : "il va falloir la mettre en non disponible
 * sauf pour l'admin"). Toutes les méthodes exigent donc SecurityUtils.isAdmin()
 * pour l'instant ; il suffira de retirer cette restriction sur les méthodes
 * de lecture/enchère (pas la création, qui doit rester admin) pour l'ouvrir
 * au public plus tard.
 *
 * La vérification "peut payer" avant d'enchérir est une SIMULATION : elle
 * compare l'enchère au solde stocké sur le compte (Utilisateur.solde, réglé
 * par l'admin), sans aucun débit ni prestataire de paiement réel — voir
 * la note dans Utilisateur.solde. Un vrai contrôle de solvabilité (Mobile
 * Money, carte bancaire...) nécessiterait une intégration externe, prévue
 * pour une prochaine version.
 */
@Service
public class EnchereService {

    private final EnchereRepository enchereRepository;
    private final EnchereOffreRepository offreRepository;
    private final ProduitRepository produitRepository;
    private final UtisateurRepository utilisateurRepository;

    public EnchereService(EnchereRepository enchereRepository,
                           EnchereOffreRepository offreRepository,
                           ProduitRepository produitRepository,
                           UtisateurRepository utilisateurRepository) {
        this.enchereRepository = enchereRepository;
        this.offreRepository = offreRepository;
        this.produitRepository = produitRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    private void garderReserveAdmin() {
        if (!SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Les enchères sont en cours de test — pour le moment réservées à l'administrateur");
        }
    }

    @Transactional
    public EnchereDto creer(Long produitId, Double prixDepart, LocalDateTime dateDebut, LocalDateTime dateFin) {
        garderReserveAdmin();
        if (dateFin.isBefore(dateDebut) || dateFin.isEqual(dateDebut)) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        if (prixDepart == null || prixDepart <= 0 || prixDepart >= produit.getPrix_produit()) {
            throw new IllegalArgumentException("Le prix de départ doit être positif et inférieur au prix normal du produit");
        }
        Enchere e = new Enchere();
        e.setProduit(produit);
        e.setPrixDepart(prixDepart);
        e.setPrixActuel(prixDepart);
        e.setDateDebut(dateDebut);
        e.setDateFin(dateFin);
        return toDto(enchereRepository.save(e));
    }

    public List<EnchereDto> lister() {
        garderReserveAdmin();
        return enchereRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void supprimer(Long id) {
        garderReserveAdmin();
        enchereRepository.deleteById(id);
    }

    /**
     * Enchérir : vérifie que l'enchère est EN_COURS, que le montant dépasse
     * le prix actuel, et que l'utilisateur a "les moyens de payer" (solde
     * simulé) — exactement le principe demandé.
     */
    @Transactional
    public EnchereDto encherir(Long enchereId, Double montant) {
        garderReserveAdmin(); // sera retiré (mais pas de creer()/supprimer()) quand la fonctionnalité s'ouvrira au public
        Enchere e = enchereRepository.findById(enchereId)
                .orElseThrow(() -> new RuntimeException("Enchère introuvable"));
        if (!"EN_COURS".equals(calculerStatut(e))) {
            throw new IllegalStateException("Cette enchère n'est pas ouverte aux offres en ce moment");
        }
        if (montant == null || montant <= e.getPrixActuel()) {
            throw new IllegalArgumentException("Votre offre doit être supérieure au prix actuel (" + e.getPrixActuel() + " FCFA)");
        }
        Long userId = SecurityUtils.getCurrentUserId();
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        if (user.getSolde() == null || user.getSolde() < montant) {
            throw new IllegalStateException(
                    "Solde insuffisant pour cette offre (solde actuel : " + (user.getSolde() == null ? 0 : user.getSolde()) + " FCFA). "
                  + "Aucune somme n'est réellement débitée — ceci ne fait que vérifier que vous pourriez payer.");
        }
        EnchereOffre offre = new EnchereOffre();
        offre.setEnchere(e);
        offre.setUtilisateur(user);
        offre.setMontant(montant);
        offreRepository.save(offre);

        e.setPrixActuel(montant);
        e.setMeilleurEncherisseur(user);
        return toDto(enchereRepository.save(e));
    }

    /** A_VENIR tant que dateDebut n'est pas atteinte, EN_COURS entre les deux bornes, TERMINEE après dateFin. */
    private String calculerStatut(Enchere e) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(e.getDateDebut())) return "A_VENIR";
        if (now.isAfter(e.getDateFin())) return "TERMINEE";
        return "EN_COURS";
    }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private EnchereDto toDto(Enchere e) {
        EnchereDto dto = new EnchereDto();
        dto.setId(e.getId());
        dto.setProduitId(e.getProduit().getId_produit());
        dto.setProduitNom(e.getProduit().getNom_produit());
        dto.setProduitImage(e.getProduit().getImageBase64());
        dto.setPrixNormal(e.getProduit().getPrix_produit());
        dto.setPrixDepart(e.getPrixDepart());
        dto.setPrixActuel(e.getPrixActuel());
        if (e.getMeilleurEncherisseur() != null) {
            dto.setMeilleurEncherisseurNom(e.getMeilleurEncherisseur().getPrenom() + " " + e.getMeilleurEncherisseur().getNom());
        }
        dto.setDateDebut(e.getDateDebut().format(FMT));
        dto.setDateFin(e.getDateFin().format(FMT));
        dto.setStatut(calculerStatut(e));
        dto.setNbOffres(offreRepository.findByEnchere_IdOrderByMontantDesc(e.getId()).size());
        return dto;
    }
}
