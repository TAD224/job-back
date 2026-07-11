package com.odc.plateforme_emploi.controller;

import com.odc.plateforme_emploi.dto.*;
import com.odc.plateforme_emploi.service.OffreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offres")
@RequiredArgsConstructor
public class OffreController {

    private final OffreService offreService;

    // PUBLIC — toutes les offres actives
    @GetMapping
    public ResponseEntity<ApiResponse<List<OffreResponse>>> getToutesLesOffres() {
        return ResponseEntity.ok(
            ApiResponse.success("Offres récupérées",
                offreService.getToutesLesOffres())
        );
    }

    // PUBLIC — une offre par ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OffreResponse>> getOffreById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("Offre trouvée", offreService.getOffreById(id))
        );
    }

    // PUBLIC — recherche par mot clé
    @GetMapping("/recherche")
    public ResponseEntity<ApiResponse<List<OffreResponse>>> rechercherOffres(
            @RequestParam String keyword) {
        return ResponseEntity.ok(
            ApiResponse.success("Résultats",
                offreService.rechercherOffres(keyword))
        );
    }

    // RECRUTEUR — créer une offre
    @PostMapping
    @PreAuthorize("hasRole('RECRUTEUR')")
    public ResponseEntity<ApiResponse<OffreResponse>> creerOffre(
            @Valid @RequestBody OffreRequest request,
            Authentication authentication) {
        OffreResponse response = offreService.creerOffre(
            request, authentication.getName()
        );
        return ResponseEntity.ok(ApiResponse.success("Offre créée !", response));
    }

    // RECRUTEUR — mes offres
    @GetMapping("/mes-offres")
    @PreAuthorize("hasRole('RECRUTEUR')")
    public ResponseEntity<ApiResponse<List<OffreResponse>>> getMesOffres(
            Authentication authentication) {
        return ResponseEntity.ok(
            ApiResponse.success("Mes offres",
                offreService.getOffresRecruteur(authentication.getName()))
        );
    }

    // RECRUTEUR — modifier une offre
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUTEUR')")
    public ResponseEntity<ApiResponse<OffreResponse>> modifierOffre(
            @PathVariable Long id,
            @Valid @RequestBody OffreRequest request,
            Authentication authentication) {
        OffreResponse response = offreService.modifierOffre(
            id, request, authentication.getName()
        );
        return ResponseEntity.ok(ApiResponse.success("Offre modifiée !", response));
    }

    // RECRUTEUR — supprimer une offre
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RECRUTEUR')")
    public ResponseEntity<ApiResponse<Void>> supprimerOffre(
            @PathVariable Long id,
            Authentication authentication) {
        offreService.supprimerOffre(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Offre supprimée !"));
    }
}
