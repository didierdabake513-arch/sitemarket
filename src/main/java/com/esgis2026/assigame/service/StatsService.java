package com.esgis2026.assigame.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.esgis2026.assigame.dto.CategorySalesDto;
import com.esgis2026.assigame.dto.MonthlyRevenueDto;
import com.esgis2026.assigame.dto.StatsSummaryDto;
import com.esgis2026.assigame.entity.Commande;
import com.esgis2026.assigame.entity.LigneCommande;
import com.esgis2026.assigame.entity.StatutCommande;
import com.esgis2026.assigame.repository.CommandeRepository;
import com.esgis2026.assigame.repository.ProduitRepository;
import com.esgis2026.assigame.repository.UtisateurRepository;
import com.esgis2026.assigame.util.SecurityUtils;

/** Statistiques globales du site — vue réservée à l'administrateur. */
@Service
public class StatsService {

    private void reserveAdmin() {
        if (!SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Réservé à l'administrateur");
        }
    }

    private final ProduitRepository produitRepository;
    private final CommandeRepository commandeRepository;
    private final UtisateurRepository utilisateurRepository;

    public StatsService(
            ProduitRepository produitRepository,
            CommandeRepository commandeRepository,
            UtisateurRepository utilisateurRepository) {
        this.produitRepository = produitRepository;
        this.commandeRepository = commandeRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public StatsSummaryDto getSummary() {
        reserveAdmin();
        double revenue = commandeRepository.findAll().stream()
                .filter(c -> c.getStatut() != StatutCommande.ANNULEE)
                .mapToDouble(Commande::getMontant_total)
                .sum();
        return new StatsSummaryDto(
                produitRepository.findAll().stream().filter(p -> !"SUPPRIME".equals(p.getStatut())).count(),
                commandeRepository.count(),
                utilisateurRepository.count(),
                revenue);
    }

    public List<MonthlyRevenueDto> getRevenueMonthly(int year) {
        reserveAdmin();
        Map<Integer, Double> byMonth = new HashMap<>();
        for (Commande c : commandeRepository.findAll()) {
            if (c.getStatut() == StatutCommande.ANNULEE || c.getDate_commande() == null) {
                continue;
            }
            if (c.getDate_commande().getYear() != year) {
                continue;
            }
            int month = c.getDate_commande().getMonthValue();
            byMonth.merge(month, c.getMontant_total(), Double::sum);
        }
        List<MonthlyRevenueDto> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            result.add(new MonthlyRevenueDto(m, byMonth.getOrDefault(m, 0.0)));
        }
        return result;
    }

    public List<CategorySalesDto> getSalesByCategory() {
        reserveAdmin();
        Map<String, Long> counts = new HashMap<>();
        for (Commande c : commandeRepository.findAll()) {
            if (c.getStatut() == StatutCommande.ANNULEE || c.getLignes() == null) {
                continue;
            }
            for (LigneCommande ligne : c.getLignes()) {
                String cat = ligne.getProduit().getCategorieProduit() != null
                        ? ligne.getProduit().getCategorieProduit().getNom_categorieproduit()
                        : "Autre";
                counts.merge(cat, (long) ligne.getQuantite(), Long::sum);
            }
        }
        return counts.entrySet().stream()
                .map(e -> new CategorySalesDto(e.getKey(), e.getValue()))
                .toList();
    }
}
