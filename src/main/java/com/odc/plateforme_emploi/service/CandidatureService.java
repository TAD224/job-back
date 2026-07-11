package com.odc.plateforme_emploi.service;

import com.odc.plateforme_emploi.dto.CandidatureResponse;
import com.odc.plateforme_emploi.entity.*;
import com.odc.plateforme_emploi.exception.BadRequestException;
import com.odc.plateforme_emploi.exception.DuplicateResourceException;
import com.odc.plateforme_emploi.exception.InvalidFileException;
import com.odc.plateforme_emploi.exception.ResourceNotFoundException;
import com.odc.plateforme_emploi.exception.UnauthorizedOperationException;
import com.odc.plateforme_emploi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final CandidatRepository candidatRepository;
    private final OffreRepository offreRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;

    /**
     * Soumission d'une candidature complète.
     * Règles : le CV est obligatoire ; la lettre de motivation est optionnelle,
     * sous forme de fichier OU de texte (les deux peuvent être fournis, aucun n'est requis).
     */
    @Transactional
    public CandidatureResponse postuler(Long offreId,
                                         MultipartFile cv,
                                         MultipartFile lettreMotivationFichier,
                                         String lettreMotivationTexte,
                                         String telephoneContact,
                                         LocalDate dateDisponibilite,
                                         String emailCandidat) {

        Candidat candidat = candidatRepository.findByEmail(emailCandidat)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Candidat non trouvé pour l'email : " + emailCandidat +
                ". Assurez-vous d'être connecté en tant que CANDIDAT."
            ));

        Offre offre = offreRepository.findById(offreId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Offre non trouvée avec l'id : " + offreId
            ));

        if (offre.getStatut() != Offre.StatutOffre.ACTIVE) {
            throw new BadRequestException("Cette offre n'accepte plus de candidatures.");
        }

        if (candidatureRepository.existsByCandidatIdAndOffreId(candidat.getId(), offre.getId())) {
            throw new DuplicateResourceException("Vous avez déjà postulé à cette offre !");
        }

        if (cv == null || cv.isEmpty()) {
            throw new InvalidFileException("Le CV est obligatoire pour postuler.");
        }
        if (telephoneContact == null || telephoneContact.isBlank()) {
            throw new BadRequestException("Le numéro de téléphone est obligatoire.");
        }
        if (dateDisponibilite == null) {
            throw new BadRequestException("La date de disponibilité est obligatoire.");
        }

        String cvPath = fileStorageService.sauvegarderCV(cv);

        String lettrePath = null;
        if (lettreMotivationFichier != null && !lettreMotivationFichier.isEmpty()) {
            lettrePath = fileStorageService.sauvegarderLettreMotivation(lettreMotivationFichier);
        }

        Candidature candidature = new Candidature();
        candidature.setCandidat(candidat);
        candidature.setOffre(offre);
        candidature.setCvPath(cvPath);
        candidature.setLettreMotivationPath(lettrePath);
        candidature.setLettreMotivation(lettreMotivationTexte);
        candidature.setTelephoneContact(telephoneContact);
        candidature.setDateDisponibilite(dateDisponibilite);

        Candidature saved = candidatureRepository.save(candidature);
        log.info("Candidature #{} créée : candidat={}, offre={}", saved.getId(), candidat.getId(), offre.getId());

        // Notifier le recruteur — un échec d'envoi ne doit pas annuler la candidature déjà enregistrée
        try {
            emailService.envoyerNotificationNouvelleCandidature(
                offre.getRecruteur().getEmail(),
                offre.getRecruteur().getPrenom(),
                candidat.getPrenom() + " " + candidat.getNom(),
                offre.getTitre()
            );
        } catch (Exception e) {
            log.warn("Candidature #{} enregistrée mais notification recruteur échouée", saved.getId(), e);
        }

        return toResponse(saved);
    }

    // Mes candidatures (candidat)
    public List<CandidatureResponse> getMesCandidatures(String emailCandidat) {
        Candidat candidat = candidatRepository.findByEmail(emailCandidat)
            .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé !"));
        return candidatureRepository.findByCandidatId(candidat.getId())
            .stream().map(this::toResponse)
            .collect(Collectors.toList());
    }

    // Candidatures reçues pour une offre (recruteur, doit être le propriétaire de l'offre)
    public List<CandidatureResponse> getCandidaturesOffre(Long offreId, String emailRecruteur) {
        Offre offre = offreRepository.findById(offreId)
            .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée !"));

        if (!offre.getRecruteur().getEmail().equals(emailRecruteur)) {
            throw new UnauthorizedOperationException(
                "Vous ne pouvez consulter que les candidatures de vos propres offres."
            );
        }

        return candidatureRepository.findByOffreId(offreId)
            .stream().map(this::toResponse)
            .collect(Collectors.toList());
    }

    // Changer le statut d'une candidature (recruteur, doit être le propriétaire de l'offre concernée)
    public CandidatureResponse changerStatut(Long id,
                                              Candidature.StatutCandidature statut,
                                              String emailRecruteur) {
        Candidature candidature = candidatureRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée !"));

        if (!candidature.getOffre().getRecruteur().getEmail().equals(emailRecruteur)) {
            throw new UnauthorizedOperationException(
                "Vous ne pouvez modifier que les candidatures de vos propres offres."
            );
        }

        candidature.setStatut(statut);
        Candidature saved = candidatureRepository.save(candidature);
        log.info("Candidature #{} passée au statut {}", id, statut);

        // Le candidat n'est notifié que pour une décision définitive (accepté/refusé) :
        // le passage à "VUE" est un simple suivi interne, pas une réponse qui mérite un email.
        if (statut == Candidature.StatutCandidature.ACCEPTEE || statut == Candidature.StatutCandidature.REFUSEE) {
            try {
                emailService.envoyerNotificationChangementStatut(
                    candidature.getCandidat().getEmail(),
                    candidature.getCandidat().getPrenom(),
                    candidature.getOffre().getTitre(),
                    candidature.getOffre().getRecruteur().getEntreprise(),
                    statut == Candidature.StatutCandidature.ACCEPTEE
                );
            } catch (Exception e) {
                log.warn("Candidature #{} mise à jour mais notification candidat échouée", id, e);
            }
        }

        return toResponse(saved);
    }

    // Annuler une candidature (candidat propriétaire uniquement, tant qu'elle n'a pas encore reçu de décision définitive)
    @Transactional
    public void annulerCandidature(Long id, String emailCandidat) {
        Candidature candidature = candidatureRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée !"));

        if (!candidature.getCandidat().getEmail().equals(emailCandidat)) {
            throw new UnauthorizedOperationException(
                "Vous ne pouvez annuler que vos propres candidatures."
            );
        }

        if (candidature.getStatut() == Candidature.StatutCandidature.ACCEPTEE
                || candidature.getStatut() == Candidature.StatutCandidature.REFUSEE) {
            throw new BadRequestException(
                "Impossible d'annuler une candidature qui a déjà reçu une réponse du recruteur."
            );
        }

        candidatureRepository.delete(candidature);
        log.info("Candidature #{} annulée par le candidat {}", id, emailCandidat);
    }

    // Chargement du CV d'une candidature — accessible au candidat propriétaire
    // ou au recruteur propriétaire de l'offre concernée.
    public org.springframework.core.io.Resource chargerCvCandidature(Long candidatureId, String emailDemandeur) {
        Candidature c = verifierAccesCandidature(candidatureId, emailDemandeur);
        return fileStorageService.chargerCV(c.getCvPath());
    }

    public org.springframework.core.io.Resource chargerLettreCandidature(Long candidatureId, String emailDemandeur) {
        Candidature c = verifierAccesCandidature(candidatureId, emailDemandeur);
        if (c.getLettreMotivationPath() == null) {
            throw new ResourceNotFoundException("Aucune lettre de motivation fichier pour cette candidature.");
        }
        return fileStorageService.chargerLettreMotivation(c.getLettreMotivationPath());
    }

    private Candidature verifierAccesCandidature(Long candidatureId, String emailDemandeur) {
        Candidature c = candidatureRepository.findById(candidatureId)
            .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée !"));

        boolean estLeCandidat = c.getCandidat().getEmail().equals(emailDemandeur);
        boolean estLeRecruteur = c.getOffre().getRecruteur().getEmail().equals(emailDemandeur);

        if (!estLeCandidat && !estLeRecruteur) {
            throw new UnauthorizedOperationException(
                "Vous n'avez pas accès aux documents de cette candidature."
            );
        }
        return c;
    }

    // Convertir Candidature → CandidatureResponse
    private CandidatureResponse toResponse(Candidature c) {
        CandidatureResponse response = new CandidatureResponse();
        response.setId(c.getId());
        response.setStatut(c.getStatut());
        response.setDateCandidature(c.getDateCandidature());
        response.setLettreMotivation(c.getLettreMotivation());
        response.setLettreMotivationPath(c.getLettreMotivationPath());
        response.setCvPath(c.getCvPath());
        response.setTelephoneContact(c.getTelephoneContact());
        response.setDateDisponibilite(c.getDateDisponibilite());
        response.setCandidatId(c.getCandidat().getId());
        response.setNomCandidat(c.getCandidat().getNom());
        response.setPrenomCandidat(c.getCandidat().getPrenom());
        response.setEmailCandidat(c.getCandidat().getEmail());
        response.setOffreId(c.getOffre().getId());
        response.setTitreOffre(c.getOffre().getTitre());
        response.setEntreprise(c.getOffre().getRecruteur().getEntreprise());
        return response;
    }
}
