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
@Table(name = "typeutilisateur")
public class TypeUtilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_typeutilisateur;

    @Column(nullable = true, length = 100)
    private String nom_utilisateur;

    @Column(nullable = true,length = 100)
    private String description;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id_typeutilisateur == null) ? 0 : id_typeutilisateur.hashCode());
        result = prime * result + ((nom_utilisateur == null) ? 0 : nom_utilisateur.hashCode());
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
        TypeUtilisateur other = (TypeUtilisateur) obj;
        if (id_typeutilisateur == null) {
            if (other.id_typeutilisateur != null)
                return false;
        } else if (!id_typeutilisateur.equals(other.id_typeutilisateur))
            return false;
        if (nom_utilisateur == null) {
            if (other.nom_utilisateur != null)
                return false;
        } else if (!nom_utilisateur.equals(other.nom_utilisateur))
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
        return "TypeUtilisateur [id_typeutilisateur=" + id_typeutilisateur + ", nom_utilisateur=" + nom_utilisateur
                + ", description=" + description + "]";
    }

}
