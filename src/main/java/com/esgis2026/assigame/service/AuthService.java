package com.esgis2026.assigame.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esgis2026.assigame.dto.AuthResponse;
import com.esgis2026.assigame.dto.LoginRequest;
import com.esgis2026.assigame.dto.RegisterRequest;
import com.esgis2026.assigame.entity.TypeUtilisateur;
import com.esgis2026.assigame.entity.Utilisateur;
import com.esgis2026.assigame.mapper.EntityMapper;
import com.esgis2026.assigame.repository.TypeUtilisateurRepository;
import com.esgis2026.assigame.repository.UtisateurRepository;
import com.esgis2026.assigame.security.JwtService;

@Service
public class AuthService {

    private final UtisateurRepository utilisateurRepository;
    private final TypeUtilisateurRepository typeUtilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UtisateurRepository utilisateurRepository,
            TypeUtilisateurRepository typeUtilisateurRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.typeUtilisateurRepository = typeUtilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        Utilisateur user = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email ou mot de passe incorrect"));
        if ("BLOQUE".equalsIgnoreCase(user.getStatut())) {
            throw new IllegalArgumentException("Compte bloqué");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getMotdepasse())) {
            throw new IllegalArgumentException("Email ou mot de passe incorrect");
        }
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }
        TypeUtilisateur clientType = typeUtilisateurRepository.findByRoleName("CLIENT")
                .orElseGet(() -> {
                    TypeUtilisateur t = new TypeUtilisateur();
                    t.setNom_utilisateur("CLIENT");
                    t.setDescription("Client");
                    return typeUtilisateurRepository.save(t);
                });

        Utilisateur user = new Utilisateur();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        user.setTelephone(request.getTelephone());
        user.setMotdepasse(passwordEncoder.encode(request.getPassword()));
        user.setRegion(request.getRegion());
        user.setDate_creation(LocalDateTime.now());
        user.setStatut("ACTIF");
        user.setTypeUtilisateur(clientType);
        utilisateurRepository.save(user);
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(Utilisateur user) {
        String role = user.getTypeUtilisateur().getNom_utilisateur().toUpperCase();
        String token = jwtService.generateToken(user.getId(), user.getEmail(), role);
        return new AuthResponse(token, EntityMapper.toUserDto(user));
    }
}
