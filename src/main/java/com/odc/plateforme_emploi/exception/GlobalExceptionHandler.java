package com.odc.plateforme_emploi.exception;

import com.odc.plateforme_emploi.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

/**
 * Intercepte toutes les exceptions levées par les contrôleurs/services de l'application
 * et les transforme en réponses JSON structurées ({@link ApiResponse}) avec le bon code HTTP.
 *
 * Sans cette classe, Spring Boot renvoyait un 500 "Whitelabel Error Page" pour absolument
 * toutes les erreurs métier (offre non trouvée, accès refusé, email déjà utilisé...),
 * ce qui empêche le frontend de distinguer les cas et d'afficher les bonnes pages
 * (404 / 403 / erreur serveur) ou les bons messages toast.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ============ 404 — Ressource introuvable ============
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Ressource introuvable [{}] : {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ============ 409 — Conflit / doublon ============
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(
            DuplicateResourceException ex, HttpServletRequest request) {
        log.warn("Conflit [{}] : {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ============ 403 — Action non autorisée (métier) ============
    @ExceptionHandler(UnauthorizedOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            UnauthorizedOperationException ex, HttpServletRequest request) {
        log.warn("Accès refusé [{}] : {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ============ 403 — Action non autorisée (Spring Security @PreAuthorize) ============
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Accès refusé par Spring Security [{}] : {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Vous n'avez pas les droits nécessaires pour cette action."));
    }

    // ============ 400 — Fichier invalide ============
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidFile(
            InvalidFileException ex, HttpServletRequest request) {
        log.warn("Fichier invalide [{}] : {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ============ 400 — Fichier trop volumineux (dépassement de la limite Spring) ============
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.warn("Fichier trop volumineux [{}]", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Le fichier dépasse la taille maximale autorisée (5 Mo)."));
    }

    // ============ 400 — Requête invalide (métier) ============
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            BadRequestException ex, HttpServletRequest request) {
        log.warn("Requête invalide [{}] : {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ============ 400 — Erreurs de validation @Valid sur les DTO ============
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("Validation échouée [{}] : {}", request.getRequestURI(), errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.validationError("Des champs sont invalides.", errors));
    }

    // ============ 400 — Token d'activation invalide/déjà utilisé ============
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidToken(
            InvalidTokenException ex, HttpServletRequest request) {
        log.warn("Token invalide [{}] : {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.errorWithCode(ex.getMessage(), "TOKEN_INVALID"));
    }

    // ============ 400 — Token d'activation expiré ============
    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpiredToken(
            ExpiredTokenException ex, HttpServletRequest request) {
        log.warn("Token expiré [{}] : {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.errorWithCode(ex.getMessage(), "TOKEN_EXPIRED"));
    }

    // ============ 403 — Compte pas encore activé ============
    @ExceptionHandler(AccountNotActivatedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountNotActivated(
            AccountNotActivatedException ex, HttpServletRequest request) {
        log.warn("Compte non activé [{}] : {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.errorWithCode(ex.getMessage(), "ACCOUNT_NOT_ACTIVATED"));
    }

    // ============ 401 — Identifiants incorrects ============
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Échec d'authentification [{}]", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Email ou mot de passe incorrect."));
    }

    // ============ 403 — Compte désactivé ============
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleDisabled(
            DisabledException ex, HttpServletRequest request) {
        log.warn("Compte désactivé [{}]", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Ce compte est désactivé ou en attente d'activation."));
    }

    // ============ 500 — Tout le reste (filet de sécurité) ============
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(
            Exception ex, HttpServletRequest request) {
        log.error("Erreur interne inattendue [{}]", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Une erreur interne est survenue. Veuillez réessayer plus tard."));
    }
}
