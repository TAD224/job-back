package com.odc.plateforme_emploi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OffreRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotBlank(message = "La localisation est obligatoire")
    private String localisation;

    private String typeContrat;   // CDI, CDD, Stage, Freelance

    private String niveauEtude;

    private String salaire;

    private LocalDateTime dateExpiration;
}
