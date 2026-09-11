package com.esgis2026.assigame.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.esgis2026.assigame.dto.CommandeDto;
import com.esgis2026.assigame.dto.CommandeItemDto;
import com.esgis2026.assigame.dto.ProduitDto;
import com.esgis2026.assigame.dto.UserDto;
import com.esgis2026.assigame.entity.Commande;
import com.esgis2026.assigame.entity.LigneCommande;
import com.esgis2026.assigame.entity.Produit;
import com.esgis2026.assigame.entity.ProduitImage;
import com.esgis2026.assigame.entity.Utilisateur;

public final class EntityMapper {

    private EntityMapper() {
    }

    public static UserDto toUserDto(Utilisateur u) {
        String role = u.getTypeUtilisateur() != null ? u.getTypeUtilisateur().getNom_utilisateur() : "CLIENT";
        boolean bloque = "BLOQUE".equalsIgnoreCase(u.getStatut());
        return UserDto.builder()
                .id(u.getId())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .email(u.getEmail())
                .telephone(u.getTelephone())
                .role(role != null ? role.toUpperCase() : "CLIENT")
                .bloque(bloque)
                .dateNaissance(u.getDate_naissance())
                .adresse(u.getAdresse())
                .region(u.getRegion())
                .vendeurProprietaireId(u.getVendeurProprietaire() != null ? u.getVendeurProprietaire().getId() : null)
                .vendeurProprietaireNom(u.getVendeurProprietaire() != null
                        ? u.getVendeurProprietaire().getPrenom() + " " + u.getVendeurProprietaire().getNom() : null)
                .solde(u.getSolde())
                .build();
    }

    public static ProduitDto toProduitDto(Produit p) {
        ProduitDto dto = new ProduitDto();
        dto.setId(p.getId_produit());
        dto.setNom(p.getNom_produit());
        dto.setPrix(p.getPrix_produit());
        dto.setStock(p.getStock() != null ? p.getStock() : 0);
        dto.setEmoji(p.getEmoji() != null ? p.getEmoji() : "📦");
        dto.setDescription(p.getDescription_produit());
        dto.setPrixBarre(p.getPrix_barre());
        dto.setEnPromotion(Boolean.TRUE.equals(p.getEn_promotion()));
        dto.setMisEnAvant(Boolean.TRUE.equals(p.getMis_en_avant()));
        dto.setStatut(p.getStatut());
        dto.setImageBase64(p.getImageBase64());
        dto.setImages(p.getImages().stream().map(ProduitImage::getImageBase64).collect(Collectors.toList()));
        if (p.getCategorieProduit() != null) {
            dto.setCategorie(p.getCategorieProduit().getNom_categorieproduit());
        }
        if (p.getVendeur() != null) {
            dto.setVendeurId(p.getVendeur().getId());
            dto.setVendeurNom(p.getVendeur().getPrenom() + " " + p.getVendeur().getNom());
        }
        return dto;
    }

    public static CommandeDto toCommandeDto(Commande c) {
        return toCommandeDto(c, false);
    }

    /**
     * @param includeCode expose le code de livraison (QR) dans le DTO —
     *   uniquement pour l'acheteur qui vient de passer commande et pour le
     *   livreur qui doit vérifier ce qu'il scanne. Jamais dans les listes
     *   générales (admin, vendeur) pour limiter la circulation de ce code.
     */
    public static CommandeDto toCommandeDto(Commande c, boolean includeCode) {
        CommandeDto dto = new CommandeDto();
        dto.setId(c.getId());
        dto.setReference(c.getReference());
        if (includeCode) {
            dto.setCodeLivraison(c.getCodeLivraison());
        }
        if (c.getLivreur() != null) {
            dto.setLivreurId(c.getLivreur().getId());
            dto.setLivreurNom(c.getLivreur().getPrenom() + " " + c.getLivreur().getNom());
        }
        dto.setTarifLivraison(c.getTarifLivraison());
        if (c.getDateExpedition() != null) {
            dto.setDateExpedition(c.getDateExpedition().toString());
        }
        if (c.getUtilisateur() != null) {
            dto.setClientId(c.getUtilisateur().getId());
            dto.setClientEmail(c.getUtilisateur().getEmail());
            dto.setClientNom(c.getUtilisateur().getPrenom() + " " + c.getUtilisateur().getNom());
        } else {
            /* Commande passée sans compte ("invité") */
            dto.setClientNom(c.getNomClientInvite() != null ? c.getNomClientInvite() : "Client invité");
        }
        dto.setMontantTotal(c.getMontant_total());
        dto.setStatut(c.getStatut() != null ? c.getStatut().name() : "EN_ATTENTE");
        dto.setDateCommande(c.getDate_commande() != null ? c.getDate_commande().toString() : null);
        dto.setNbArticles(c.getNb_articles());
        dto.setAdresseLivraison(c.getAdresseLivraison());
        dto.setTelephone(c.getTelephoneLivraison());
        dto.setModePaiement(c.getModePaiement());
        List<LigneCommande> lignes = c.getLignes();
        if (lignes != null && !lignes.isEmpty()) {
            dto.setItems(lignes.stream()
                    .map(l -> {
                        CommandeItemDto item = new CommandeItemDto(l.getProduit().getId_produit(), l.getQuantite());
                        item.setProduitNom(l.getProduit().getNom_produit());
                        if (l.getProduit().getVendeur() != null) {
                            item.setVendeurNom(l.getProduit().getVendeur().getPrenom() + " " + l.getProduit().getVendeur().getNom());
                        }
                        return item;
                    })
                    .collect(Collectors.toList()));
            dto.setEmoji(lignes.get(0).getProduit().getEmoji());
            String vendeurs = lignes.stream()
                    .map(l -> l.getProduit().getVendeur())
                    .filter(java.util.Objects::nonNull)
                    .map(v -> v.getPrenom() + " " + v.getNom())
                    .distinct()
                    .collect(Collectors.joining(", "));
            dto.setVendeursNoms(vendeurs.isEmpty() ? null : vendeurs);
        }
        return dto;
    }
}
