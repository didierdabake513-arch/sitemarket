package com.esgis2026.assigame.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "categorieProduit")
public class CategorieProduit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idcategorie_produit;

   
    @Column (unique = true, nullable = false, length = 40)
    private String nom_categorieproduit;
    
    @Column(unique = false, nullable = true, length = 100)
    private String description;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((idcategorie_produit == null) ? 0 : idcategorie_produit.hashCode());
        result = prime * result + ((nom_categorieproduit == null) ? 0 : nom_categorieproduit.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CategorieProduit other = (CategorieProduit) obj;
        if (idcategorie_produit == null) {
            if (other.idcategorie_produit != null)
                return false;
        } else if (!idcategorie_produit.equals(other.idcategorie_produit))
            return false;
        if (nom_categorieproduit == null) {
            if (other.nom_categorieproduit != null)
                return false;
        } else if (!nom_categorieproduit.equals(other.nom_categorieproduit))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "categorieProduit [idcategorie_produit=" + idcategorie_produit
                + ", nom_categorieproduit=" + nom_categorieproduit
                + ", description=" + description + "]";
    }
    

}

