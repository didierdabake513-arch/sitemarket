package com.esgis2026.assigame.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esgis2026.assigame.dto.PromoBannerDto;
import com.esgis2026.assigame.service.PromoBannerService;

@RestController
@RequestMapping("/api/promo-banner")
public class PromoBannerController {

    private final PromoBannerService service;

    public PromoBannerController(PromoBannerService service) {
        this.service = service;
    }

    /** Public : lu par l'accueil pour afficher la bannière. */
    @GetMapping
    public PromoBannerDto get() {
        return service.get();
    }

    /** Réservé à l'admin : modification du texte/lien/visibilité. */
    @PutMapping
    public PromoBannerDto update(@RequestBody PromoBannerDto dto) {
        return service.update(dto);
    }
}
