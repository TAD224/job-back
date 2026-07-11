package com.odc.plateforme_emploi.controller;

import com.odc.plateforme_emploi.dto.ApiResponse;
import com.odc.plateforme_emploi.entity.Candidat;
import com.odc.plateforme_emploi.exception.ResourceNotFoundException;
import com.odc.plateforme_emploi.exception.UnauthorizedOperationException;
import com.odc.plateforme_emploi.repository.CandidatRepository;
import com.odc.plateforme_emploi.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/candidat")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CANDIDAT')")
public class CandidatController {

    private final CandidatRepository candidatRepository;
    private final FileStorageService fileStorageService;

    // Upload CV de profil (celui pré-rempli lors d'une candidature)
    @PostMapping("/cv")
    public ResponseEntity<ApiResponse<String>> uploadCV(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        Candidat candidat = candidatRepository
            .findByEmail(authentication.getName())
            .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé !"));

        String ancienCv = candidat.getCvPath();

        String fileName = fileStorageService.sauvegarderCV(file);
        candidat.setCvPath(fileName);
        candidatRepository.save(candidat);

        // On supprime l'ancien fichier seulement après le succès de l'enregistrement
        // du nouveau, pour ne jamais se retrouver sans CV en cas d'erreur intermédiaire.
        if (ancienCv != null) {
            fileStorageService.supprimerCV(ancienCv);
        }

        return ResponseEntity.ok(
            ApiResponse.success("CV uploadé avec succès !", fileName)
        );
    }

    // Télécharger son propre CV de profil (aucun autre utilisateur ne peut y accéder ainsi ;
    // pour télécharger le CV joint à une candidature précise, voir CandidatureController
    // qui vérifie la propriété candidat/recruteur au niveau de la candidature).
    @GetMapping("/cv/{fileName:.+}")
    public ResponseEntity<Resource> downloadCV(
            @PathVariable String fileName, Authentication authentication) {

        Candidat candidat = candidatRepository
            .findByEmail(authentication.getName())
            .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé !"));

        if (!fileName.equals(candidat.getCvPath())) {
            throw new UnauthorizedOperationException("Vous ne pouvez télécharger que votre propre CV.");
        }

        Resource resource = fileStorageService.chargerCV(fileName);
        return construireReponseTelechargement(resource);
    }

    private ResponseEntity<Resource> construireReponseTelechargement(Resource resource) {
        String contentType = "application/octet-stream";
        String nom = resource.getFilename();
        if (nom != null && nom.toLowerCase().endsWith(".pdf")) {
            contentType = "application/pdf";
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + nom + "\"")
            .body(resource);
    }
}
