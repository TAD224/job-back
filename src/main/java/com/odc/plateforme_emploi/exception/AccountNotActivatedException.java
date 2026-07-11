package com.odc.plateforme_emploi.exception;

/**
 * Levée lors d'une tentative de connexion sur un compte dont l'e-mail n'a pas
 * encore été confirmé. Mappée en HTTP 403 avec le code "ACCOUNT_NOT_ACTIVATED"
 * pour que le frontend propose directement le renvoi d'un lien d'activation.
 */
public class AccountNotActivatedException extends RuntimeException {

    public AccountNotActivatedException(String message) {
        super(message);
    }
}
