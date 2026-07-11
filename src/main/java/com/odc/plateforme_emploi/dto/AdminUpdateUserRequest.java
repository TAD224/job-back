package com.odc.plateforme_emploi.dto;

import com.odc.plateforme_emploi.entity.Utilisateur;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminUpdateUserRequest {
    @NotBlank
    private String nom;

    @NotBlank
    private String prenom;

    @Email
    @NotBlank
    private String email;

    private Utilisateur.Role role;
    private boolean actif;

    // Candidat
    private String telephone;
    private String competences;

    // Recruteur
    private String entreprise;
    private String secteur;
}
