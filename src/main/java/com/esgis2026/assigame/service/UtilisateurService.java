package com.esgis2026.assigame.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esgis2026.assigame.dto.AuthResponse;
import com.esgis2026.assigame.dto.NotificationsRequest;
import com.esgis2026.assigame.dto.PasswordChangeRequest;
import com.esgis2026.assigame.dto.UserDto;
import com.esgis2026.assigame.entity.TypeUtilisateur;
import com.esgis2026.assigame.entity.Utilisateur;
import com.esgis2026.assigame.mapper.EntityMapper;
import com.esgis2026.assigame.repository.TypeUtilisateurRepository;
import com.esgis2026.assigame.repository.UtisateurRepository;
import com.esgis2026.assigame.security.JwtService;
import com.esgis2026.assigame.util.SecurityUtils;

@Service
public class UtilisateurService {

    private final UtisateurRepository utilisateurRepository;
    private final TypeUtilisateurRepository typeUtilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UtilisateurService(
            UtisateurRepository utilisateurRepository,
            TypeUtilisateurRepository typeUtilisateurRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.typeUtilisateurRepository = typeUtilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** Liste de tous les comptes — réservé à l'administrateur. */
    public List<UserDto> findAll() {
        if (!SecurityUtils.isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException("Réservé à l'administrateur");
        }
        return utilisateurRepository.findAll().stream()
                .map(EntityMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public UserDto findById(Long id) {
        return EntityMapper.toUserDto(getUser(id));
    }

    @Transactional
    public UserDto updateProfile(Long id, Map<String, Object> profile) {
        Utilisateur user = getUser(id);
        if (profile.containsKey("nom")) {
            user.setNom((String) profile.get("nom"));
        }
        if (profile.containsKey("prenom")) {
            user.setPrenom((String) profile.get("prenom"));
        }
        if (profile.containsKey("email")) {
            user.setEmail((String) profile.get("email"));
        }
        if (profile.containsKey("telephone")) {
            user.setTelephone((String) profile.get("telephone"));
        }
        if (profile.containsKey("dateNaissance")) {
            user.setDate_naissance((String) profile.get("dateNaissance"));
        }
        if (profile.containsKey("adresse")) {
            user.setAdresse((String) profile.get("adresse"));
        }
        if (profile.containsKey("region")) {
            user.setRegion((String) profile.get("region"));
        }
        return EntityMapper.toUserDto(utilisateurRepository.save(user));
    }

    @Transactional
    public void changePassword(Long id, PasswordChangeRequest request) {
        Utilisateur user = getUser(id);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getMotdepasse())) {
            throw new IllegalArgumentException("Ancien mot de passe incorrect");
        }
        user.setMotdepasse(passwordEncoder.encode(request.getNewPassword()));
        utilisateurRepository.save(user);
    }

    @Transactional
    public void updateNotifications(Long id, NotificationsRequest request) {
        Utilisateur user = getUser(id);
        user.setNotifications_json(String.valueOf(request.getPreferences()));
        utilisateurRepository.save(user);
    }

    /** Bloquer/débloquer un compte — réservé à l'administrateur. */
    @Transactional
    public void toggleBlock(Long id, boolean bloquer) {
        if (!SecurityUtils.isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException("Réservé à l'administrateur");
        }
        Utilisateur user = getUser(id);
        user.setStatut(bloquer ? "BLOQUE" : "ACTIF");
        utilisateurRepository.save(user);
    }

    /** Réglage du solde simulé (vérification "peut payer" avant enchère) — réservé à l'admin. */
    @Transactional
    public UserDto setSolde(Long id, Double solde) {
        if (!SecurityUtils.isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException("Réservé à l'administrateur");
        }
        Utilisateur user = getUser(id);
        user.setSolde(solde != null ? solde : 0.0);
        return EntityMapper.toUserDto(utilisateurRepository.save(user));
    }

    /** Suppression d'un compte — soit son propre compte (DELETE /me), soit,
     *  pour l'administrateur, celui de n'importe quel utilisateur. */
    @Transactional
    public void delete(Long id) {
        Long currentId = SecurityUtils.getCurrentUserId();
        boolean estSoiMeme = currentId != null && currentId.equals(id);
        if (!estSoiMeme && !SecurityUtils.isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Vous ne pouvez supprimer que votre propre compte");
        }
        if (!utilisateurRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur introuvable");
        }
        utilisateurRepository.deleteById(id);
    }

    @Transactional
    public AuthResponse becomeVendeur(Long id) {
        Utilisateur user = getUser(id);
        String role = user.getTypeUtilisateur() != null
                ? user.getTypeUtilisateur().getNom_utilisateur()
                : "CLIENT";
        if (role != null && !"CLIENT".equalsIgnoreCase(role)) {
            throw new IllegalArgumentException("Seuls les clients peuvent devenir vendeur");
        }
        TypeUtilisateur vendeurType = typeUtilisateurRepository.findByRoleName("VENDEUR")
                .orElseGet(() -> {
                    TypeUtilisateur t = new TypeUtilisateur();
                    t.setNom_utilisateur("VENDEUR");
                    t.setDescription("Vendeur");
                    return typeUtilisateurRepository.save(t);
                });
        user.setTypeUtilisateur(vendeurType);
        Utilisateur saved = utilisateurRepository.save(user);
        UserDto dto = EntityMapper.toUserDto(saved);
        String token = jwtService.generateToken(saved.getId(), saved.getEmail(), "VENDEUR");
        return new AuthResponse(token, dto);
    }

    private Utilisateur getUser(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }
}
