package com.esgis2026.assigame.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import com.esgis2026.assigame.entity.CategorieProduit;
import com.esgis2026.assigame.service.CategorieProduitService;

@RestController
@RequestMapping("/api/categorieproduit")
public class CategorieProduitController {

    private final CategorieProduitService categorieProduitService;

    public CategorieProduitController(CategorieProduitService categorieProduitService){
        this.categorieProduitService = categorieProduitService;
    }

    @GetMapping("/list")
    public List<CategorieProduit> getAllCategorieProduit() {
        return categorieProduitService.getAllCategorieProduits();
    }

    @PostMapping("/add")
    public CategorieProduit addCategorieProduit(@RequestBody CategorieProduit categorieProduit) {
        return categorieProduitService.createcaCategorieProduit(categorieProduit);
    }

}
