package com.odc.plateforme_emploi.exception;

/**
 * Levée lorsqu'un utilisateur authentifié tente une action qui ne lui appartient pas
 * (ex : un recruteur qui essaie de modifier l'offre d'un autre recruteur).
 * Mappée automatiquement en HTTP 403 FORBIDDEN par le GlobalExceptionHandler.
 */
public class UnauthorizedOperationException extends RuntimeException {

    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
