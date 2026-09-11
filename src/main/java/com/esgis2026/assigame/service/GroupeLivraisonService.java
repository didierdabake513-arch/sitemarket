package com.esgis2026.assigame.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esgis2026.assigame.dto.GroupeLivraisonDto;
import com.esgis2026.assigame.entity.GroupeLivraison;
import com.esgis2026.assigame.entity.Utilisateur;
import com.esgis2026.assigame.repository.GroupeLivraisonRepository;
import com.esgis2026.assigame.repository.UtisateurRepository;
import com.esgis2026.assigame.util.SecurityUtils;

/**
 * Groupes de livraison entre vendeurs — libre d'accès, aucune validation
 * admin nécessaire pour rejoindre (comme convenu). Seule la création est
 * réservée aux vendeurs qui ont déjà au moins un livreur (ils apportent le
 * service, les autres viennent juste l'utiliser).
 */
@Service
public class GroupeLivraisonService {

    private final GroupeLivraisonRepository groupeRepository;
    private final UtisateurRepository utilisateurRepository;

    public GroupeLivraisonService(GroupeLivraisonRepository groupeRepository,
                                   UtisateurRepository utilisateurRepository) {
        this.groupeRepository = groupeRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional
    public GroupeLivraisonDto creer(String nom) {
        if (!SecurityUtils.isVendeur()) {
            throw new AccessDeniedException("Réservé aux vendeurs");
        }
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom du groupe est requis");
        }
        Long vendeurId = SecurityUtils.getCurrentUserId();
        long nbLivreurs = utilisateurRepository.findByVendeurProprietaire_Id(vendeurId).size();
        if (nbLivreurs == 0) {
            throw new IllegalStateException("Vous devez avoir au moins un livreur pour créer un groupe de livraison");
        }
        Utilisateur vendeur = utilisateurRepository.findById(vendeurId)
                .orElseThrow(() -> new RuntimeException("Vendeur introuvable"));
        GroupeLivraison g = new GroupeLivraison();
        g.setNom(nom.trim());
        g.setCreateur(vendeur);
        return toDto(groupeRepository.save(g), vendeurId);
    }

    /** Tous les groupes existants, pour qu'un vendeur puisse choisir lequel rejoindre. */
    public List<GroupeLivraisonDto> listerTous() {
        if (!SecurityUtils.isVendeur()) {
            throw new AccessDeniedException("Réservé aux vendeurs");
        }
        Long vendeurId = SecurityUtils.getCurrentUserId();
        return groupeRepository.findAll().stream()
                .map(g -> toDto(g, vendeurId))
                .collect(Collectors.toList());
    }

    /** Les groupes créés par le vendeur connecté OU qu'il a rejoints. */
    public List<GroupeLivraisonDto> mesGroupes() {
        if (!SecurityUtils.isVendeur()) {
            throw new AccessDeniedException("Réservé aux vendeurs");
        }
        Long vendeurId = SecurityUtils.getCurrentUserId();
        return groupeRepository.findAll().stream()
                .filter(g -> g.getCreateur().getId().equals(vendeurId)
                        || g.getMembres().stream().anyMatch(m -> m.getId().equals(vendeurId)))
                .map(g -> toDto(g, vendeurId))
                .collect(Collectors.toList());
    }

    @Transactional
    public GroupeLivraisonDto rejoindre(Long groupeId) {
        if (!SecurityUtils.isVendeur()) {
            throw new AccessDeniedException("Réservé aux vendeurs");
        }
        Long vendeurId = SecurityUtils.getCurrentUserId();
        GroupeLivraison g = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new RuntimeException("Groupe introuvable"));
        if (g.getCreateur().getId().equals(vendeurId)) {
            throw new IllegalStateException("Vous êtes déjà le créateur de ce groupe");
        }
        Utilisateur vendeur = utilisateurRepository.findById(vendeurId)
                .orElseThrow(() -> new RuntimeException("Vendeur introuvable"));
        g.getMembres().add(vendeur);
        return toDto(groupeRepository.save(g), vendeurId);
    }

    @Transactional
    public void quitter(Long groupeId) {
        if (!SecurityUtils.isVendeur()) {
            throw new AccessDeniedException("Réservé aux vendeurs");
        }
        Long vendeurId = SecurityUtils.getCurrentUserId();
        GroupeLivraison g = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new RuntimeException("Groupe introuvable"));
        if (g.getCreateur().getId().equals(vendeurId)) {
            throw new IllegalStateException("Le créateur ne peut pas quitter son propre groupe (il peut être supprimé à la place)");
        }
        g.getMembres().removeIf(m -> m.getId().equals(vendeurId));
        groupeRepository.save(g);
    }

    private GroupeLivraisonDto toDto(GroupeLivraison g, Long currentVendeurId) {
        GroupeLivraisonDto dto = new GroupeLivraisonDto();
        dto.setId(g.getId());
        dto.setNom(g.getNom());
        dto.setCreateurId(g.getCreateur().getId());
        dto.setCreateurNom(g.getCreateur().getPrenom() + " " + g.getCreateur().getNom());
        dto.setEstMembre(g.getCreateur().getId().equals(currentVendeurId)
                || g.getMembres().stream().anyMatch(m -> m.getId().equals(currentVendeurId)));
        dto.setNbMembres(g.getMembres().size());
        dto.setNbLivreurs(utilisateurRepository.findByVendeurProprietaire_Id(g.getCreateur().getId()).size());
        dto.setMembresNoms(g.getMembres().stream().map(m -> m.getPrenom() + " " + m.getNom()).collect(Collectors.toList()));
        return dto;
    }
}
