package com.odc.plateforme_emploi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidatures")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", nullable = false)
    private Candidat candidat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offre_id", nullable = false)
    private Offre offre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCandidature statut = StatutCandidature.EN_ATTENTE;

    @Column(name = "date_candidature")
    private LocalDateTime dateCandidature = LocalDateTime.now();

    // Lettre de motivation saisie directement en texte (optionnelle)
    @Column(columnDefinition = "TEXT")
    private String lettreMotivation;

    // CV obligatoire, spécifique à cette candidature (peut différer du CV de profil)
    @Column(name = "cv_path", nullable = false)
    private String cvPath;

    // Lettre de motivation téléversée en fichier (optionnelle, alternative au texte)
    @Column(name = "lettre_motivation_path")
    private String lettreMotivationPath;

    // Téléphone de contact pour cette candidature précise (peut différer du profil)
    @Column(name = "telephone_contact")
    private String telephoneContact;

    // Date à laquelle le candidat est disponible pour commencer
    @Column(name = "date_disponibilite")
    private LocalDate dateDisponibilite;

    public enum StatutCandidature {
        EN_ATTENTE, VUE, ACCEPTEE, REFUSEE
    }
}