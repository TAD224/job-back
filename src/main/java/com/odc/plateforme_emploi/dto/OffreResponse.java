package com.odc.plateforme_emploi.dto;

import com.odc.plateforme_emploi.entity.Offre;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OffreResponse {

    private Long id;
    private String titre;
    private String description;
    private String localisation;
    private String typeContrat;
    private String niveauEtude;
    private String salaire;
    private Offre.StatutOffre statut;
    private LocalDateTime datePublication;
    private LocalDateTime dateExpiration;

    // Infos recruteur
    private Long recruteurId;
    private String entreprise;
    private String secteur;

    // Nombre de candidatures reçues
    private int nombreCandidatures;
}