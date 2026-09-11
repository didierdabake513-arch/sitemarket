package com.esgis2026.assigame.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esgis2026.assigame.dto.PromoBannerDto;
import com.esgis2026.assigame.entity.PromoBanner;
import com.esgis2026.assigame.repository.PromoBannerRepository;
import com.esgis2026.assigame.util.SecurityUtils;

@Service
public class PromoBannerService {

    private final PromoBannerRepository repository;

    public PromoBannerService(PromoBannerRepository repository) {
        this.repository = repository;
    }

    public PromoBannerDto get() {
        return toDto(getOrCreate());
    }

    @Transactional
    public PromoBannerDto update(PromoBannerDto dto) {
        if (!SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Réservé à l'administrateur");
        }
        PromoBanner banner = getOrCreate();
        if (dto.getTitre() != null) banner.setTitre(dto.getTitre());
        if (dto.getSousTitre() != null) banner.setSousTitre(dto.getSousTitre());
        if (dto.getTexteBouton() != null) banner.setTexteBouton(dto.getTexteBouton());
        if (dto.getLienBouton() != null) banner.setLienBouton(dto.getLienBouton());
        if (dto.getActif() != null) banner.setActif(dto.getActif());
        if (dto.getMisEnAvantTitre() != null) banner.setMisEnAvantTitre(dto.getMisEnAvantTitre());
        if (dto.getMisEnAvantTexte() != null) banner.setMisEnAvantTexte(dto.getMisEnAvantTexte());
        return toDto(repository.save(banner));
    }

    private PromoBanner getOrCreate() {
        return repository.findById(1L).orElseGet(() -> repository.save(new PromoBanner()));
    }

    private PromoBannerDto toDto(PromoBanner b) {
        PromoBannerDto dto = new PromoBannerDto();
        dto.setTitre(b.getTitre());
        dto.setSousTitre(b.getSousTitre());
        dto.setTexteBouton(b.getTexteBouton());
        dto.setLienBouton(b.getLienBouton());
        dto.setActif(b.getActif());
        dto.setMisEnAvantTitre(b.getMisEnAvantTitre());
        dto.setMisEnAvantTexte(b.getMisEnAvantTexte());
        return dto;
    }
}
