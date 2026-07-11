package com.odc.plateforme_emploi.dto;

import com.odc.plateforme_emploi.entity.Utilisateur;
import lombok.Data;

@Data
public class UtilisateurResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private Utilisateur.Role role;
    private boolean actif;
    private boolean enabled;

    // Champs candidat (null si recruteur)
    private String telephone;
    private String competences;

    // Champs recruteur (null si candidat)
    private String entreprise;
    private String secteur;
}