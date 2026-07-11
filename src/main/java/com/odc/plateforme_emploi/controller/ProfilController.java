package com.odc.plateforme_emploi.controller;

import com.odc.plateforme_emploi.dto.*;
import com.odc.plateforme_emploi.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profil")
@RequiredArgsConstructor
public class ProfilController {

    private final UtilisateurService utilisateurService;

    // Voir son propre profil
    @GetMapping
    public ResponseEntity<ApiResponse<UtilisateurResponse>> monProfil(
            Authentication authentication) {
        return ResponseEntity.ok(
            ApiResponse.success("Profil récupéré",
                utilisateurService.getMonProfil(authentication.getName()))
        );
    }

    // Modifier son propre profil
    @PutMapping
    public ResponseEntity<ApiResponse<UtilisateurResponse>> modifierMonProfil(
            @Valid @RequestBody UpdateProfilRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
            ApiResponse.success("Profil mis à jour !",
                utilisateurService.modifierMonProfil(authentication.getName(), request))
        );
    }
}
