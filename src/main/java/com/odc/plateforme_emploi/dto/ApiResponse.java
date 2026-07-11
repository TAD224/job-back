package com.odc.plateforme_emploi.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    // Erreurs de validation par champ, ex: {"email": "Email invalide"}. Null si non applicable.
    private Map<String, String> errors;

    // Code machine-lisible pour les cas où le frontend doit distinguer des erreurs similaires
    // sans parser le message (ex: "TOKEN_EXPIRED" vs "TOKEN_INVALID"). Null si non applicable.
    private String code;

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(boolean success, String message, T data, Map<String, String> errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    // Réponse succès avec données
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    // Réponse succès sans données
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    // Réponse erreur simple
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    // Réponse erreur avec code machine-lisible
    public static <T> ApiResponse<T> errorWithCode(String message, String code) {
        ApiResponse<T> response = new ApiResponse<>(false, message, null);
        response.setCode(code);
        return response;
    }

    // Réponse erreur avec détail des champs invalides
    public static <T> ApiResponse<T> validationError(String message, Map<String, String> errors) {
        return new ApiResponse<>(false, message, null, errors);
    }
}