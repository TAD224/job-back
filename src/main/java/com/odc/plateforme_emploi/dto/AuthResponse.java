package com.odc.plateforme_emploi.dto;

import com.odc.plateforme_emploi.entity.Utilisateur;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private Utilisateur.Role role;

    public AuthResponse(String token, Long id, String nom,
                        String prenom, String email, Utilisateur.Role role) {
        this.token = token;
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
    }
}