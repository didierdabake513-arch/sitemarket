package com.esgis2026.assigame.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.esgis2026.assigame.dto.ProduitDto;
import com.esgis2026.assigame.service.ProduitService;

@RestController
@RequestMapping("/api/produits")
public class ProduitController {

    private final ProduitService produitService;

    public ProduitController(ProduitService produitService) {
        this.produitService = produitService;
    }

    @GetMapping
    public List<ProduitDto> getAll() {
        return produitService.findAll();
    }

    /** Dashboard Admin : tous les produits, y compris ceux supprimés (grisés + restaurables). */
    @GetMapping("/admin/all")
    public List<ProduitDto> getAllForAdmin() {
        return produitService.findAllForAdmin();
    }

    @GetMapping("/{id}")
    public ProduitDto getById(@PathVariable Long id) {
        return produitService.findById(id);
    }

    /** Produits du vendeur actuellement connecté uniquement. */
    @GetMapping("/me")
    public List<ProduitDto> getMyProducts() {
        return produitService.findMyProducts();
    }

    @PostMapping
    public ProduitDto create(@RequestBody ProduitDto dto) {
        return produitService.create(dto);
    }

    @PutMapping("/{id}")
    public ProduitDto update(@PathVariable Long id, @RequestBody ProduitDto dto) {
        return produitService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        produitService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Annule une suppression — réservé à l'admin. */
    @PutMapping("/{id}/restaurer")
    public ProduitDto restaurer(@PathVariable Long id) {
        return produitService.restaurer(id);
    }

    /** Met en avant / retire de la mise en avant sur l'accueil — réservé à l'admin. */
    @PutMapping("/{id}/mettre-en-avant")
    public ProduitDto setMisEnAvant(@PathVariable Long id, @RequestParam boolean value) {
        return produitService.setMisEnAvant(id, value);
    }
}
