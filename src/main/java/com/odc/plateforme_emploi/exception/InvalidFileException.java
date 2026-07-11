package com.odc.plateforme_emploi.exception;

/**
 * Levée lorsqu'un fichier uploadé (CV, lettre de motivation) ne respecte pas
 * les règles de validation (extension, taille, nom manquant...).
 * Mappée automatiquement en HTTP 400 BAD REQUEST par le GlobalExceptionHandler.
 */
public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }
}
