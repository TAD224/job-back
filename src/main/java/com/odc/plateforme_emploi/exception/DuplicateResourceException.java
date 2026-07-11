package com.odc.plateforme_emploi.exception;

/**
 * Levée lorsqu'une action créerait un doublon interdit
 * (email déjà utilisé, candidature déjà envoyée pour la même offre...).
 * Mappée automatiquement en HTTP 409 CONFLICT par le GlobalExceptionHandler.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
