package com.esgis2026.assigame.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.esgis2026.assigame.entity.Utilisateur;

@Repository
public interface UtisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM Utilisateur u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);

    java.util.List<Utilisateur> findByVendeurProprietaire_Id(Long vendeurId);

    /* Le champ de l'entité s'appelle littéralement "nom_utilisateur" (avec
       underscore, pas en camelCase) — une requête dérivée classique
       (findByTypeUtilisateur_NomUtilisateur...) ne le résout pas, d'où la
       requête JPQL explicite, comme dans TypeUtilisateurRepository. */
    @Query("SELECT u FROM Utilisateur u WHERE LOWER(u.typeUtilisateur.nom_utilisateur) = LOWER(:nom)")
    java.util.List<Utilisateur> findByTypeUtilisateur_NomUtilisateurIgnoreCase(@Param("nom") String nom);
}
