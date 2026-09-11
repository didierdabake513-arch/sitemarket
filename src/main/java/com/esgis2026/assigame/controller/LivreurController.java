package com.esgis2026.assigame.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.esgis2026.assigame.dto.LivreurDto;
import com.esgis2026.assigame.service.LivreurService;

@RestController
@RequestMapping("/api/livreurs")
public class LivreurController {

    private final LivreurService livreurService;

    public LivreurController(LivreurService livreurService) {
        this.livreurService = livreurService;
    }

    @PostMapping
    public LivreurDto creer(@RequestBody LivreurDto dto) {
        return livreurService.creer(dto);
    }

    @GetMapping("/mes-livreurs")
    public List<LivreurDto> mesLivreurs() {
        return livreurService.mesLivreurs();
    }

    /** Livreurs assignables : les siens + ceux des groupes rejoints. */
    @GetMapping("/disponibles")
    public List<LivreurDto> disponibles() {
        return livreurService.livreursDisponibles();
    }

    @PutMapping("/{id}/bloquer")
    public void toggleBlocage(@PathVariable Long id, @RequestParam boolean bloque) {
        livreurService.toggleBlocage(id, bloque);
    }
}
