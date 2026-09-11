package com.esgis2026.assigame.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.esgis2026.assigame.entity.CategorieProduit;

@Repository
public interface CategorieProduitRepository extends JpaRepository<CategorieProduit, Long> {

    @Query("SELECT c FROM CategorieProduit c WHERE LOWER(c.nom_categorieproduit) = LOWER(:nom)")
    Optional<CategorieProduit> findByNom(@Param("nom") String nom);
}
