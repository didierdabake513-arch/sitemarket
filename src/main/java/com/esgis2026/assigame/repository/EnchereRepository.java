package com.esgis2026.assigame.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esgis2026.assigame.entity.Enchere;

@Repository
public interface EnchereRepository extends JpaRepository<Enchere, Long> {
}
