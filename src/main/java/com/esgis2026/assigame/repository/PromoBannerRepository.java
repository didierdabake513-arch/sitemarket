package com.esgis2026.assigame.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esgis2026.assigame.entity.PromoBanner;

@Repository
public interface PromoBannerRepository extends JpaRepository<PromoBanner, Long> {
}
