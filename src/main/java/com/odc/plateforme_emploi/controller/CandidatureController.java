package com.odc.plateforme_emploi.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.odc.plateforme_emploi.dto.ApiResponse;
import com.odc.plateforme_emploi.dto.CandidatureResponse;
import com.odc.plateforme_emploi.entity.Candidature;
import com.odc.plateforme_emploi.service.CandidatureService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/candidatures")
@RequiredArgsConstructor
public class CandidatureController {

    private final CandidatureService candidatureService;

    // CANDIDAT — postuler à une offre. CV obligatoire, lettre optionnelle (fichier OU texte).
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CANDIDAT')")
    public ResponseEntity<ApiResponse<CandidatureResponse>> postuler(
            @RequestParam Long offreId,
            @RequestParam("cv") MultipartFile cv,
            @RequestParam(value = "lettreMotivationFichier", required = false) MultipartFile lettreMotivationFichier,
            @RequestParam(value = "lettreMotivationTexte", required = false) String lettreMotivationTexte,
            @RequestParam String telephoneContact,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDisponibilite,
            Authentication authentication) {
        CandidatureResponse response = candidatureService.postuler(
            offreId, cv, lettreMotivationFichier, lettreMotivationTexte,
            telephoneContact, dateDisponibilite, authentication.getName()
        );
        return ResponseEntity.ok(ApiResponse.success("Candidature envoyée !", response));
    }

    // CANDIDAT — mes candidatures
    @GetMapping("/mes-candidatures")
    public ResponseEntity<ApiResponse<List<CandidatureResponse>>> getMesCandidatures(
            Authentication authentication) {
        return ResponseEntity.ok(
            ApiResponse.success("Mes candidatures",
                candidatureService.getMesCandidatures(authentication.getName()))
        );
    }

    // RECRUTEUR — candidatures reçues pour une offre
    @GetMapping("/offre/{offreId}")
    @PreAuthorize("hasRole('RECRUTEUR')")
    public ResponseEntity<ApiResponse<List<CandidatureResponse>>> getCandidaturesOffre(
            @PathVariable Long offreId,
            Authentication authentication) {
        return ResponseEntity.ok(
            ApiResponse.success("Candidatures reçues",
                candidatureService.getCandidaturesOffre(offreId, authentication.getName()))
        );
    }

    // RECRUTEUR — changer le statut d'une candidature
    @PutMapping("/{id}/statut")
    @PreAuthorize("hasRole('RECRUTEUR')")
    public ResponseEntity<ApiResponse<CandidatureResponse>> changerStatut(
            @PathVariable Long id,
            @RequestParam Candidature.StatutCandidature statut,
            Authentication authentication) {
        CandidatureResponse response = candidatureService.changerStatut(id, statut, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Statut de la candidature mis à jour !", response));
    }

    // CANDIDAT — annuler une de ses candidatures (tant qu'elle n'a pas reçu de décision définitive)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CANDIDAT')")
    public ResponseEntity<ApiResponse<Void>> annulerCandidature(
            @PathVariable Long id, Authentication authentication) {
        candidatureService.annulerCandidature(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Candidature annulée.", null));
    }

    // Télécharger le CV joint à une candidature précise
    // (le candidat propriétaire OU le recruteur propriétaire de l'offre concernée)
    @GetMapping("/{id}/cv")
    public ResponseEntity<Resource> telechargerCvCandidature(
            @PathVariable Long id, Authentication authentication) {
        Resource resource = candidatureService.chargerCvCandidature(id, authentication.getName());
        return construireReponseTelechargement(resource);
    }

    // Télécharger la lettre de motivation (fichier) jointe à une candidature précise
    @GetMapping("/{id}/lettre")
    public ResponseEntity<Resource> telechargerLettreCandidature(
            @PathVariable Long id, Authentication authentication) {
        Resource resource = candidatureService.chargerLettreCandidature(id, authentication.getName());
        return construireReponseTelechargement(resource);
    }

    private ResponseEntity<Resource> construireReponseTelechargement(Resource resource) {
        String nom = resource.getFilename();
        String contentType = (nom != null && nom.toLowerCase().endsWith(".pdf"))
            ? "application/pdf" : "application/octet-stream";
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nom + "\"")
            .body(resource);
    }
}