package com.esgis2026.assigame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esgis2026.assigame.entity.GroupeLivraison;

@Repository
public interface GroupeLivraisonRepository extends JpaRepository<GroupeLivraison, Long> {

    List<GroupeLivraison> findByCreateur_Id(Long createurId);

    List<GroupeLivraison> findByMembres_Id(Long membreId);
}
