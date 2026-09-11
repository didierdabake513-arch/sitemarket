package com.esgis2026.assigame.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esgis2026.assigame.dto.EnchereDto;
import com.esgis2026.assigame.service.EnchereService;

@RestController
@RequestMapping("/api/encheres")
public class EnchereController {

    private final EnchereService enchereService;

    public EnchereController(EnchereService enchereService) {
        this.enchereService = enchereService;
    }

    @PostMapping
    public EnchereDto creer(@RequestBody Map<String, Object> body) {
        Long produitId = Long.valueOf(String.valueOf(body.get("produitId")));
        Double prixDepart = Double.valueOf(String.valueOf(body.get("prixDepart")));
        LocalDateTime dateDebut = LocalDateTime.parse(String.valueOf(body.get("dateDebut")));
        LocalDateTime dateFin = LocalDateTime.parse(String.valueOf(body.get("dateFin")));
        return enchereService.creer(produitId, prixDepart, dateDebut, dateFin);
    }

    @GetMapping
    public List<EnchereDto> lister() {
        return enchereService.lister();
    }

    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Long id) {
        enchereService.supprimer(id);
    }

    @PutMapping("/{id}/encherir")
    public EnchereDto encherir(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Double montant = Double.valueOf(String.valueOf(body.get("montant")));
        return enchereService.encherir(id, montant);
    }
}
