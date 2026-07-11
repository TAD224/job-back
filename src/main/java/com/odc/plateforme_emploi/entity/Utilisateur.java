package com.odc.plateforme_emploi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "utilisateurs")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nom;

    @NotBlank
    @Column(nullable = false)
    private String prenom;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public enum Role {
        CANDIDAT, RECRUTEUR, ADMIN
    }
    @Column(nullable = false)
    private boolean actif = true;

    // Devient true uniquement après confirmation de l'e-mail via le lien d'activation.
    // Distinct de 'actif' qui sert à la suspension par un administrateur.
    @Column(nullable = false)
    private boolean enabled = false;
}