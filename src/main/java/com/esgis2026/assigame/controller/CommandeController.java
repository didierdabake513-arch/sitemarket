package com.esgis2026.assigame.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esgis2026.assigame.dto.CommandeDto;
import com.esgis2026.assigame.dto.CreateCommandeRequest;
import com.esgis2026.assigame.dto.StatutUpdateRequest;
import com.esgis2026.assigame.service.CommandeService;
import com.esgis2026.assigame.util.SecurityUtils;

@RestController
@RequestMapping("/api/commandes")
public class CommandeController {

    private final CommandeService commandeService;

    public CommandeController(CommandeService commandeService) {
        this.commandeService = commandeService;
    }

    @GetMapping
    public List<CommandeDto> getAll() {
        return commandeService.findAll();
    }

    @GetMapping("/me")
    public List<CommandeDto> getMyOrders() {
        return commandeService.findByUserId(SecurityUtils.getCurrentUserId());
    }

    /** Commandes contenant au moins un produit du VENDEUR connecté. */
    @GetMapping("/vendeur/me")
    public List<CommandeDto> getVendeurOrders() {
        return commandeService.findByVendeurConnecte();
    }

    /**
     * Création d'une commande (bouton "Confirmer ma commande" côté front).
     * C'est cette route qui manquait — son absence faisait que le front
     * recevait une erreur 405 du serveur et le bouton restait bloqué en
     * "Traitement en cours…".
     */
    @PostMapping
    public ResponseEntity<CommandeDto> create(@RequestBody CreateCommandeRequest request) {
        CommandeDto created = commandeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/statut")
    public CommandeDto updateStatut(@PathVariable Long id, @RequestBody StatutUpdateRequest request) {
        return commandeService.updateStatut(id, request.getStatut());
    }

    /** Le vendeur assigne un de ses livreurs à une commande. */
    @PutMapping("/{id}/assigner-livreur")
    public CommandeDto assignerLivreur(@PathVariable Long id, @org.springframework.web.bind.annotation.RequestParam Long livreurId) {
        return commandeService.assignerLivreur(id, livreurId);
    }

    /** Le vendeur propriétaire du livreur le fait partir avec tous les colis en attente. */
    @PutMapping("/livreur/{livreurId}/faire-partir")
    public List<CommandeDto> fairePartirTournee(@PathVariable Long livreurId) {
        return commandeService.faireePartirTournee(livreurId);
    }

    /** Commandes assignées au livreur connecté, encore à livrer. */
    @GetMapping("/mes-livraisons")
    public List<CommandeDto> mesLivraisons() {
        return commandeService.mesLivraisons();
    }

    /** Le livreur scanne le QR code du client : confirme la livraison. */
    @PostMapping("/scanner-livraison")
    public CommandeDto scannerLivraison(@RequestBody com.esgis2026.assigame.dto.ScanLivraisonRequest request) {
        return commandeService.confirmerLivraisonParCode(request.getCodeLivraison());
    }
}
