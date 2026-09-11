package com.esgis2026.assigame.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.esgis2026.assigame.entity.CategorieProduit;
import com.esgis2026.assigame.repository.CategorieProduitRepository;

@Service
public class CategorieProduitService  {

    final CategorieProduitRepository categorieProduitRepository;

    public CategorieProduitService(CategorieProduitRepository categorieProduitRepository) {
        this.categorieProduitRepository = categorieProduitRepository;
    }
    
    public List<CategorieProduit>getAllCategorieProduits(){
        return categorieProduitRepository.findAll();

    }

    public CategorieProduit createcaCategorieProduit(CategorieProduit categorieProduit) {
        return categorieProduitRepository.save(categorieProduit);

    }

    public void deleteCategorieProduit(Long idCategorieProduit){
        categorieProduitRepository.deleteById(idCategorieProduit);

    }

    public CategorieProduit updateCategorieProduit(Long idCategorieProduit, CategorieProduit details){
         CategorieProduit existingCategorieProduit = categorieProduitRepository.findById(idCategorieProduit)
         .orElseThrow(() -> new
                RuntimeException("CategorieProduit not found with id" + idCategorieProduit));
        existingCategorieProduit.setNom_categorieproduit(details.getNom_categorieproduit());
        existingCategorieProduit.setDescription(details.getDescription());
        return categorieProduitRepository.save(existingCategorieProduit);
    }
    
}
