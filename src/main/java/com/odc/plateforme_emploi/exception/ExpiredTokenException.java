package com.odc.plateforme_emploi.exception;

/**
 * Levée lorsqu'un token d'activation de compte a dépassé sa date d'expiration.
 * Mappée en HTTP 400 avec le code "TOKEN_EXPIRED" pour que le frontend affiche
 * la page "Lien expiré" avec un bouton "Renvoyer un nouveau lien".
 */
public class ExpiredTokenException extends RuntimeException {

    public ExpiredTokenException(String message) {
        super(message);
    }
}
