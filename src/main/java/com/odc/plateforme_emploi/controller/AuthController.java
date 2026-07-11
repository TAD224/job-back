package com.odc.plateforme_emploi.controller;

import com.odc.plateforme_emploi.dto.*;
import com.odc.plateforme_emploi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Inscription réussie !", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Connexion réussie !", response));
    }

    // Appelé quand l'utilisateur clique sur le lien reçu par e-mail
    @GetMapping("/activate")
    public ResponseEntity<ApiResponse<Void>> activate(@RequestParam String token) {
        authService.activerCompte(token);
        return ResponseEntity.ok(ApiResponse.success("Compte activé avec succès !"));
    }

    // Permet de redemander un lien si l'ancien a expiré
    @PostMapping("/resend-activation")
    public ResponseEntity<ApiResponse<Void>> resendActivation(
            @Valid @RequestBody ResendActivationRequest request) {
        authService.renvoyerActivation(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Un nouveau lien d'activation a été envoyé !"));
    }
}