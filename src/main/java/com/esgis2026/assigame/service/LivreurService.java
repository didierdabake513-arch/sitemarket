package com.esgis2026.assigame.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esgis2026.assigame.dto.LivreurDto;
import com.esgis2026.assigame.entity.Commande;
import com.esgis2026.assigame.entity.StatutCommande;
import com.esgis2026.assigame.entity.TypeUtilisateur;
import com.esgis2026.assigame.entity.Utilisateur;
import com.esgis2026.assigame.repository.CommandeRepository;
import com.esgis2026.assigame.repository.GroupeLivraisonRepository;
import com.esgis2026.assigame.repository.TypeUtilisateurRepository;
import com.esgis2026.assigame.repository.UtisateurRepository;
import com.esgis2026.assigame.util.SecurityUtils;

/**
 * Gestion des comptes LIVREUR — créés et détenus exclusivement par un
 * vendeur pour son équipe de livraison (pas d'auto-inscription possible).
 */
@Service
public class LivreurService {

    private final UtisateurRepository utilisateurRepository;
    private final TypeUtilisateurRepository typeUtilisateurRepository;
    private final CommandeRepository commandeRepository;
    private final GroupeLivraisonRepository groupeLivraisonRepository;
    private final PasswordEncoder passwordEncoder;

    public LivreurService(UtisateurRepository utilisateurRepository,
                           TypeUtilisateurRepository typeUtilisateurRepository,
                           CommandeRepository commandeRepository,
                           GroupeLivraisonRepository groupeLivraisonRepository,
                           PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.typeUtilisateurRepository = typeUtilisateurRepository;
        this.commandeRepository = commandeRepository;
        this.groupeLivraisonRepository = groupeLivraisonRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LivreurDto creer(LivreurDto dto) {
        if (!SecurityUtils.isVendeur()) {
            throw new AccessDeniedException("Réservé aux vendeurs");
        }
        if (dto.getEmail() == null || dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new IllegalArgumentException("Email et mot de passe (6 caractères minimum) sont requis");
        }
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }
        Long vendeurId = SecurityUtils.getCurrentUserId();
        Utilisateur vendeur = utilisateurRepository.findById(vendeurId)
                .orElseThrow(() -> new RuntimeException("Vendeur introuvable"));

        TypeUtilisateur livreurType = typeUtilisateurRepository.findByRoleName("LIVREUR")
                .orElseGet(() -> {
                    TypeUtilisateur t = new TypeUtilisateur();
                    t.setNom_utilisateur("LIVREUR");
                    t.setDescription("Livreur");
                    return typeUtilisateurRepository.save(t);
                });

        Utilisateur livreur = new Utilisateur();
        livreur.setNom(dto.getNom());
        livreur.setPrenom(dto.getPrenom());
        livreur.setEmail(dto.getEmail());
        livreur.setTelephone(dto.getTelephone());
        livreur.setMotdepasse(passwordEncoder.encode(dto.getPassword()));
        livreur.setDate_creation(LocalDateTime.now());
        livreur.setStatut("ACTIF");
        livreur.setTypeUtilisateur(livreurType);
        livreur.setVendeurProprietaire(vendeur);
        return toDto(utilisateurRepository.save(livreur), false);
    }

    public List<LivreurDto> mesLivreurs() {
        if (!SecurityUtils.isVendeur()) {
            throw new AccessDeniedException("Réservé aux vendeurs");
        }
        Long vendeurId = SecurityUtils.getCurrentUserId();
        return utilisateurRepository.findByVendeurProprietaire_Id(vendeurId).stream()
                .map(u -> toDto(u, false))
                .collect(Collectors.toList());
    }

    /**
     * Livreurs que le vendeur connecté peut assigner à une commande : les
     * siens, ET ceux des vendeurs dont il a rejoint le groupe de livraison
     * (livreurDeGroupe = true pour ces derniers, pour l'affichage front).
     * Triés du plus libre au moins libre (le moins de colis en attente en
     * premier) — c'est ce livreur qui recevra le colis en priorité.
     */
    public List<LivreurDto> livreursDisponibles() {
        if (!SecurityUtils.isVendeur()) {
            throw new AccessDeniedException("Réservé aux vendeurs");
        }
        Long vendeurId = SecurityUtils.getCurrentUserId();
        List<LivreurDto> resultat = new java.util.ArrayList<>();
        utilisateurRepository.findByVendeurProprietaire_Id(vendeurId)
                .forEach(u -> resultat.add(toDto(u, false)));

        groupeLivraisonRepository.findByMembres_Id(vendeurId).forEach(g -> {
            Long proprietaireId = g.getCreateur().getId();
            utilisateurRepository.findByVendeurProprietaire_Id(proprietaireId)
                    .forEach(u -> resultat.add(toDto(u, true)));
        });

        resultat.sort(java.util.Comparator.comparingInt(
                l -> l.getColisEnAttenteDepart() + l.getLivraisonsEnCours()));
        return resultat;
    }

    @Transactional
    public void toggleBlocage(Long livreurId, boolean bloque) {
        if (!SecurityUtils.isVendeur()) {
            throw new AccessDeniedException("Réservé aux vendeurs");
        }
        Utilisateur livreur = utilisateurRepository.findById(livreurId)
                .orElseThrow(() -> new RuntimeException("Livreur introuvable"));
        Long vendeurId = SecurityUtils.getCurrentUserId();
        if (livreur.getVendeurProprietaire() == null || !livreur.getVendeurProprietaire().getId().equals(vendeurId)) {
            throw new AccessDeniedException("Ce livreur ne fait pas partie de votre équipe");
        }
        livreur.setStatut(bloque ? "BLOQUE" : "ACTIF");
        utilisateurRepository.save(livreur);
    }

    private LivreurDto toDto(Utilisateur u, boolean livreurDeGroupe) {
        LivreurDto dto = new LivreurDto();
        dto.setId(u.getId());
        dto.setNom(u.getNom());
        dto.setPrenom(u.getPrenom());
        dto.setEmail(u.getEmail());
        dto.setTelephone(u.getTelephone());
        dto.setBloque("BLOQUE".equalsIgnoreCase(u.getStatut()));
        List<Commande> toutes = commandeRepository.findByLivreur_Id(u.getId());
        int enCours = (int) toutes.stream().filter(c -> c.getStatut() == StatutCommande.EXPEDIEE).count();
        int enAttente = (int) toutes.stream().filter(c -> c.getStatut() == StatutCommande.EN_ATTENTE).count();
        dto.setLivraisonsEnCours(enCours);
        dto.setColisEnAttenteDepart(enAttente);
        dto.setLivreurDeGroupe(livreurDeGroupe);
        if (livreurDeGroupe && u.getVendeurProprietaire() != null) {
            dto.setProprietaireNom(u.getVendeurProprietaire().getPrenom() + " " + u.getVendeurProprietaire().getNom());
        }
        dto.setEstimationDelai(CommandeService.estimationDelai(enAttente));
        return dto;
    }
}
