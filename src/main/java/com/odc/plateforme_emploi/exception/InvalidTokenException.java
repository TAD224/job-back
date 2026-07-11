package com.odc.plateforme_emploi.exception;

/**
 * Levée lorsqu'un token d'activation de compte n'existe pas ou a déjà été utilisé.
 * Mappée en HTTP 400 avec le code "TOKEN_INVALID" pour que le frontend affiche
 * la page dédiée (différente de la page "lien expiré").
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
