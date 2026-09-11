package com.esgis2026.assigame.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esgis2026.assigame.dto.AuthResponse;
import com.esgis2026.assigame.dto.BlockUserRequest;
import com.esgis2026.assigame.dto.NotificationsRequest;
import com.esgis2026.assigame.dto.PasswordChangeRequest;
import com.esgis2026.assigame.dto.UserDto;
import com.esgis2026.assigame.service.UtilisateurService;
import com.esgis2026.assigame.util.SecurityUtils;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public List<UserDto> getAll() {
        return utilisateurService.findAll();
    }

    @GetMapping("/me")
    public UserDto getMe() {
        return utilisateurService.findById(SecurityUtils.getCurrentUserId());
    }

    @PutMapping("/me")
    public UserDto updateMe(@RequestBody Map<String, Object> profile) {
        return utilisateurService.updateProfile(SecurityUtils.getCurrentUserId(), profile);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@RequestBody PasswordChangeRequest request) {
        utilisateurService.changePassword(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/notifications")
    public ResponseEntity<Void> updateNotifications(@RequestBody NotificationsRequest request) {
        utilisateurService.updateNotifications(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe() {
        utilisateurService.delete(SecurityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/devenir-vendeur")
    public AuthResponse becomeVendeurPut() {
        return utilisateurService.becomeVendeur(SecurityUtils.getCurrentUserId());
    }

    @PostMapping("/me/devenir-vendeur")
    public AuthResponse becomeVendeurPost() {
        return utilisateurService.becomeVendeur(SecurityUtils.getCurrentUserId());
    }

    @PutMapping("/{id}/bloquer")
    public ResponseEntity<Void> toggleBlock(@PathVariable Long id, @RequestBody BlockUserRequest request) {
        utilisateurService.toggleBlock(id, Boolean.TRUE.equals(request.getBloquer()));
        return ResponseEntity.noContent().build();
    }

    /** Réglage du solde simulé (voir Utilisateur.solde) — réservé à l'admin. */
    @PutMapping("/{id}/solde")
    public UserDto setSolde(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Double solde = Double.valueOf(String.valueOf(body.get("solde")));
        return utilisateurService.setSolde(id, solde);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        utilisateurService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
