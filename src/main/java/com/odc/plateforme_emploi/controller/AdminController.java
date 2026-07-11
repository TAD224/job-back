package com.odc.plateforme_emploi.controller;

import com.odc.plateforme_emploi.dto.*;
import com.odc.plateforme_emploi.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/utilisateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UtilisateurService utilisateurService;

    // Liste de tous les utilisateurs
    @GetMapping
    public ResponseEntity<ApiResponse<List<UtilisateurResponse>>> getTous() {
        return ResponseEntity.ok(
            ApiResponse.success("Utilisateurs récupérés",
                utilisateurService.getTousLesUtilisateurs())
        );
    }

    // Détail d'un utilisateur
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UtilisateurResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("Utilisateur trouvé",
                utilisateurService.getUtilisateurById(id))
        );
    }

    // Créer un utilisateur
    @PostMapping
    public ResponseEntity<ApiResponse<UtilisateurResponse>> creer(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(
            ApiResponse.success("Utilisateur créé !",
                utilisateurService.creerUtilisateur(request))
        );
    }

    // Modifier un utilisateur
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UtilisateurResponse>> modifier(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest request) {
        return ResponseEntity.ok(
            ApiResponse.success("Utilisateur modifié !",
                utilisateurService.adminModifierUtilisateur(id, request))
        );
    }

    // Suspendre / réactiver un utilisateur
    @PatchMapping("/{id}/statut")
    public ResponseEntity<ApiResponse<UtilisateurResponse>> changerStatut(
            @PathVariable Long id,
            @RequestParam boolean actif,
            Authentication authentication) {
        return ResponseEntity.ok(
            ApiResponse.success(actif ? "Utilisateur réactivé !" : "Utilisateur suspendu !",
                utilisateurService.changerStatutActif(id, actif, authentication.getName()))
        );
    }

    // Supprimer un utilisateur
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> supprimer(
            @PathVariable Long id,
            Authentication authentication) {
        utilisateurService.supprimerUtilisateur(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Utilisateur supprimé !"));
    }
}