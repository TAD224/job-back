package com.odc.plateforme_emploi.dto;



import com.odc.plateforme_emploi.entity.Utilisateur;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String motDePasse;

    private Utilisateur.Role role; // CANDIDAT ou RECRUTEUR

    // Champs spécifiques au candidat
    private String telephone;
    private String competences;
    private String biographie;

    // Champs spécifiques au recruteur
    private String entreprise;
    private String secteur;
    private String siteWeb;
}
