package com.odc.plateforme_emploi.exception;

/**
 * Levée lorsqu'une ressource demandée (Utilisateur, Offre, Candidature, Candidat, Recruteur...)
 * n'existe pas en base de données.
 * Mappée automatiquement en HTTP 404 NOT FOUND par le GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String ressource, Long id) {
        super(ressource + " introuvable avec l'id : " + id);
    }
}
