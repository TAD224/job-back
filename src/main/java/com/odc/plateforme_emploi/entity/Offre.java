package com.odc.plateforme_emploi.entity;



import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "offres_emploi")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Offre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String titre;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private String localisation;

    private String typeContrat;    // CDI, CDD, Stage, Freelance

    private String niveauEtude;    // Bac, Licence, Master...

    private String salaire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutOffre statut = StatutOffre.ACTIVE;

    @Column(name = "date_publication")
    private LocalDateTime datePublication = LocalDateTime.now();

    @Column(name = "date_expiration")
    private LocalDateTime dateExpiration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruteur_id", nullable = false)
    private Recruteur recruteur;

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Candidature> candidatures;

    public enum StatutOffre {
        ACTIVE, FERMEE, EXPIREE
    }
}
