package com.esgis2026.assigame.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esgis2026.assigame.dto.ProduitDto;
import com.esgis2026.assigame.entity.CategorieProduit;
import com.esgis2026.assigame.entity.Produit;
import com.esgis2026.assigame.entity.ProduitImage;
import com.esgis2026.assigame.entity.Utilisateur;
import com.esgis2026.assigame.mapper.EntityMapper;
import com.esgis2026.assigame.repository.CategorieProduitRepository;
import com.esgis2026.assigame.repository.ProduitRepository;
import com.esgis2026.assigame.repository.UtisateurRepository;
import com.esgis2026.assigame.util.SecurityUtils;

@Service
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final CategorieProduitRepository categorieProduitRepository;
    private final UtisateurRepository utilisateurRepository;

    public ProduitService(ProduitRepository produitRepository,
                           CategorieProduitRepository categorieProduitRepository,
                           UtisateurRepository utilisateurRepository) {
        this.produitRepository = produitRepository;
        this.categorieProduitRepository = categorieProduitRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /** Catalogue public : uniquement les produits actifs (jamais ceux supprimés par un admin). */
    public List<ProduitDto> findAll() {
        return produitRepository.findAll().stream()
                .filter(p -> !"SUPPRIME".equals(p.getStatut()))
                .sorted(Comparator.comparing(Produit::getId_produit).reversed())
                .map(EntityMapper::toProduitDto)
                .collect(Collectors.toList());
    }

    /** Dashboard Admin uniquement : absolument tous les produits, y compris ceux supprimés
     *  (affichés grisés avec un bouton Restaurer côté front). */
    public List<ProduitDto> findAllForAdmin() {
        if (!SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Réservé à l'administrateur");
        }
        return produitRepository.findAll().stream()
                .sorted(Comparator.comparing(Produit::getId_produit).reversed())
                .map(EntityMapper::toProduitDto)
                .collect(Collectors.toList());
    }

    public ProduitDto findById(Long id) {
        return EntityMapper.toProduitDto(getProduit(id));
    }

    /** Produits du VENDEUR actuellement connecté uniquement (hors ceux supprimés par un admin). */
    public List<ProduitDto> findMyProducts() {
        Long userId = SecurityUtils.getCurrentUserId();
        return produitRepository.findByVendeur_Id(userId).stream()
                .filter(p -> !"SUPPRIME".equals(p.getStatut()))
                .sorted(Comparator.comparing(Produit::getId_produit).reversed())
                .map(EntityMapper::toProduitDto)
                .collect(Collectors.toList());
    }

    /**
     * Création d'un produit.
     * Règle métier : seul un VENDEUR peut ajouter un produit. Un ADMIN
     * ne peut pas en créer (il peut seulement modérer : modifier ou
     * supprimer ce qui existe déjà). Le produit créé est automatiquement
     * associé au vendeur connecté — jamais transmis par le front.
     */
    @Transactional
    public ProduitDto create(ProduitDto dto) {
        if (!SecurityUtils.isVendeur()) {
            throw new AccessDeniedException("Seul un vendeur peut ajouter un produit");
        }
        Produit produit = mapToEntity(new Produit(), dto);
        produit.setDate_ajout(LocalDateTime.now());
        produit.setStatus_produit("ACTIF");
        produit.setVendeur(getCurrentVendeur());
        return EntityMapper.toProduitDto(produitRepository.save(produit));
    }

    /**
     * Modification d'un produit.
     * Réservé au VENDEUR propriétaire — l'ADMIN ne peut plus modifier les
     * produits (seulement les supprimer / restaurer, voir plus bas), pour
     * éviter qu'il n'altère le contenu d'un vendeur à son insu.
     */
    @Transactional
    public ProduitDto update(Long id, ProduitDto dto) {
        Produit produit = getProduit(id);
        ensureOwner(produit);
        mapToEntity(produit, dto);
        return EntityMapper.toProduitDto(produitRepository.save(produit));
    }

    /**
     * Suppression d'un produit — ADMIN (modération, n'importe quel produit)
     * ou VENDEUR (ses propres produits uniquement). C'est une suppression
     * "douce" : le produit est marqué SUPPRIME, jamais effacé de la base,
     * pour permettre à l'admin de revenir sur sa décision (voir restaurer()).
     */
    @Transactional
    public void delete(Long id) {
        Produit produit = getProduit(id);
        ensureOwnerOrAdmin(produit);
        produit.setStatus_produit("SUPPRIME");
        produit.setStatut("SUPPRIME");
        produitRepository.save(produit);
    }

    /** Annule une suppression — réservé à l'ADMIN. */
    @Transactional
    public ProduitDto restaurer(Long id) {
        if (!SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Réservé à l'administrateur");
        }
        Produit produit = getProduit(id);
        produit.setStatus_produit("ACTIF");
        produit.setStatut("ACTIF");
        return EntityMapper.toProduitDto(produitRepository.save(produit));
    }

    /** Met un produit en avant sur l'accueil (ou retire cette mise en avant) —
     *  décision réservée à l'ADMIN, comme convenu (le vendeur pourra
     *  seulement "demander" via la messagerie vendeur↔admin à venir). */
    @Transactional
    public ProduitDto setMisEnAvant(Long id, boolean value) {
        if (!SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Réservé à l'administrateur");
        }
        Produit produit = getProduit(id);
        produit.setMis_en_avant(value);
        return EntityMapper.toProduitDto(produitRepository.save(produit));
    }

    private void ensureOwner(Produit produit) {
        Long currentId = SecurityUtils.getCurrentUserId();
        boolean isOwner = produit.getVendeur() != null && produit.getVendeur().getId().equals(currentId);
        if (!isOwner) {
            throw new AccessDeniedException("Vous ne pouvez modifier que vos propres produits");
        }
    }

    private void ensureOwnerOrAdmin(Produit produit) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        Long currentId = SecurityUtils.getCurrentUserId();
        boolean isOwner = produit.getVendeur() != null && produit.getVendeur().getId().equals(currentId);
        if (!isOwner) {
            throw new AccessDeniedException("Vous ne pouvez gérer que vos propres produits");
        }
    }

    private Utilisateur getCurrentVendeur() {
        Long userId = SecurityUtils.getCurrentUserId();
        return utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    private Produit getProduit(Long id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
    }

    private Produit mapToEntity(Produit produit, ProduitDto dto) {
        produit.setNom_produit(dto.getNom());
        produit.setPrix_produit(dto.getPrix() != null ? dto.getPrix() : 0);
        produit.setStock(dto.getStock() != null ? dto.getStock() : 0);
        produit.setEmoji(dto.getEmoji() != null ? dto.getEmoji() : "📦");
        produit.setDescription_produit(dto.getDescription() != null ? dto.getDescription() : "");
        produit.setPrix_barre(dto.getPrixBarre());
        produit.setEn_promotion(Boolean.TRUE.equals(dto.getEnPromotion()));
        produit.setCategorieProduit(resolveCategorie(dto.getCategorie()));

        /* Galerie (plusieurs photos, jusqu'à ~10 en pratique côté front).
           On ne touche à rien si le front n'a rien envoyé (ex. modification
           d'un produit sans re-uploader ses photos) ; sinon on REMPLACE
           entièrement la galerie par la nouvelle liste reçue. La première
           image devient la "couverture" (imageBase64) utilisée partout
           ailleurs sur le site sans changement de code. */
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            produit.getImages().clear();
            int ordre = 0;
            for (String img : dto.getImages()) {
                if (img == null || img.isBlank()) continue;
                ProduitImage pi = new ProduitImage();
                pi.setProduit(produit);
                pi.setImageBase64(img);
                pi.setOrdre(ordre++);
                produit.getImages().add(pi);
            }
            if (!produit.getImages().isEmpty()) {
                produit.setImageBase64(produit.getImages().get(0).getImageBase64());
            }
        } else if (dto.getImageBase64() != null && !dto.getImageBase64().isBlank()) {
            /* Rétro-compatibilité : un appel qui n'envoie que l'ancien champ
               unique (imageBase64) continue de fonctionner comme avant. */
            produit.setImageBase64(dto.getImageBase64());
            if (produit.getImages().isEmpty()) {
                ProduitImage pi = new ProduitImage();
                pi.setProduit(produit);
                pi.setImageBase64(dto.getImageBase64());
                pi.setOrdre(0);
                produit.getImages().add(pi);
            }
        }
        return produit;
    }

    private CategorieProduit resolveCategorie(String nom) {
        String categorieNom = (nom == null || nom.isBlank()) ? "Autre" : nom;
        return categorieProduitRepository.findByNom(categorieNom)
                .orElseGet(() -> {
                    CategorieProduit cat = new CategorieProduit();
                    cat.setNom_categorieproduit(categorieNom);
                    cat.setDescription(categorieNom);
                    return categorieProduitRepository.save(cat);
                });
    }
}
