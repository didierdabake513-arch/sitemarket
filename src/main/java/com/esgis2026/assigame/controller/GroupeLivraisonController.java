package com.esgis2026.assigame.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esgis2026.assigame.dto.GroupeLivraisonDto;
import com.esgis2026.assigame.service.GroupeLivraisonService;

@RestController
@RequestMapping("/api/groupes-livraison")
public class GroupeLivraisonController {

    private final GroupeLivraisonService service;

    public GroupeLivraisonController(GroupeLivraisonService service) {
        this.service = service;
    }

    @PostMapping
    public GroupeLivraisonDto creer(@RequestBody Map<String, String> body) {
        return service.creer(body.get("nom"));
    }

    @GetMapping
    public List<GroupeLivraisonDto> listerTous() {
        return service.listerTous();
    }

    @GetMapping("/mes-groupes")
    public List<GroupeLivraisonDto> mesGroupes() {
        return service.mesGroupes();
    }

    @PostMapping("/{id}/rejoindre")
    public GroupeLivraisonDto rejoindre(@PathVariable Long id) {
        return service.rejoindre(id);
    }

    @DeleteMapping("/{id}/quitter")
    public void quitter(@PathVariable Long id) {
        service.quitter(id);
    }
}
