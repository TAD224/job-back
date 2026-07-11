package com.odc.plateforme_emploi.dto;


import com.odc.plateforme_emploi.entity.Candidature;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CandidatureResponse {

    private Long id;
    private Candidature.StatutCandidature statut;
    private LocalDateTime dateCandidature;
    private String lettreMotivation;
    private String lettreMotivationPath;
    private String cvPath;
    private String telephoneContact;
    private LocalDate dateDisponibilite;

    // Infos candidat
    private Long candidatId;
    private String nomCandidat;
    private String prenomCandidat;
    private String emailCandidat;

    // Infos offre
    private Long offreId;
    private String titreOffre;
    private String entreprise;
}
