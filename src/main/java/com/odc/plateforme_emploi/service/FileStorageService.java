package com.odc.plateforme_emploi.service;

import com.odc.plateforme_emploi.exception.InvalidFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    // Dossier racine de stockage, ex: "uploads" → uploads/cv, uploads/lettres
    @Value("${app.upload.dir}")
    private String uploadBaseDir;

    private static final List<String> EXTENSIONS_DOCUMENT = List.of(".pdf", ".doc", ".docx");
    private static final long TAILLE_MAX_OCTETS = 5 * 1024 * 1024; // 5 Mo

    public String sauvegarderCV(MultipartFile file) {
        return sauvegarder(file, "cv");
    }

    public String sauvegarderLettreMotivation(MultipartFile file) {
        return sauvegarder(file, "lettres");
    }

    public void supprimerCV(String fileName) {
        supprimer("cv", fileName);
    }

    public void supprimerLettreMotivation(String fileName) {
        supprimer("lettres", fileName);
    }

    public Resource chargerCV(String fileName) {
        return charger("cv", fileName);
    }

    public Resource chargerLettreMotivation(String fileName) {
        return charger("lettres", fileName);
    }

    // ============ Implémentation générique ============

    private String sauvegarder(MultipartFile file, String sousDossier) {
        try {
            if (file == null || file.isEmpty()) {
                throw new InvalidFileException("Le fichier est vide ou manquant.");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank() || !originalFilename.contains(".")) {
                throw new InvalidFileException("Nom de fichier invalide !");
            }

            String extension = originalFilename
                .substring(originalFilename.lastIndexOf('.'))
                .toLowerCase();

            if (!EXTENSIONS_DOCUMENT.contains(extension)) {
                throw new InvalidFileException(
                    "Format non supporté ! Formats acceptés : PDF, DOC, DOCX"
                );
            }

            if (file.getSize() > TAILLE_MAX_OCTETS) {
                throw new InvalidFileException("Fichier trop volumineux ! Taille maximale : 5 Mo");
            }

            Path uploadPath = Paths.get(uploadBaseDir, sousDossier);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID() + "_" + originalFilename;
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Fichier sauvegardé dans {} : {}", sousDossier, fileName);
            return fileName;

        } catch (IOException e) {
            log.error("Erreur E/S lors de la sauvegarde du fichier ({})", sousDossier, e);
            throw new RuntimeException("Erreur lors de la sauvegarde du fichier : " + e.getMessage());
        }
    }

    private void supprimer(String sousDossier, String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        try {
            Path filePath = Paths.get(uploadBaseDir, sousDossier).resolve(fileName);
            Files.deleteIfExists(filePath);
            log.debug("Fichier supprimé ({}) : {}", sousDossier, fileName);
        } catch (IOException e) {
            log.warn("Impossible de supprimer le fichier ({}) : {}", sousDossier, fileName, e);
        }
    }

    private Resource charger(String sousDossier, String fileName) {
        try {
            Path filePath = Paths.get(uploadBaseDir, sousDossier).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new InvalidFileException("Fichier introuvable : " + fileName);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new InvalidFileException("Chemin de fichier invalide : " + fileName);
        }
    }
}
