package com.odc.plateforme_emploi.exception;

/**
 * Levée pour toute requête syntaxiquement correcte mais sémantiquement invalide
 * (ex : rôle inexistant, valeur d'énumération incohérente).
 * Mappée automatiquement en HTTP 400 BAD REQUEST par le GlobalExceptionHandler.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
