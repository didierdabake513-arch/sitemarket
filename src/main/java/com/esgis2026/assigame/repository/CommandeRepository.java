package com.esgis2026.assigame.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.esgis2026.assigame.entity.Commande;
import com.esgis2026.assigame.entity.StatutCommande;
import com.esgis2026.assigame.entity.Utilisateur;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {

    @Query("SELECT c FROM Commande c WHERE c.utilisateur = :utilisateur ORDER BY c.date_commande DESC")
    List<Commande> findByUtilisateurOrderByDate_commandeDesc(@Param("utilisateur") Utilisateur utilisateur);

    /** Conservé pour compatibilité (n'est plus appelé — voir CommandeAutoStatutScheduler). */
    List<Commande> findByStatutAndDateExpeditionBefore(StatutCommande statut, LocalDate date);

    java.util.Optional<Commande> findByCodeLivraison(String codeLivraison);

    List<Commande> findByLivreur_Id(Long livreurId);

    List<Commande> findByLivreur_IdAndStatut(Long livreurId, StatutCommande statut);
}
