package com.odc.plateforme_emploi.service;


import com.odc.plateforme_emploi.dto.OffreRequest;
import com.odc.plateforme_emploi.dto.OffreResponse;
import com.odc.plateforme_emploi.entity.Offre;
import com.odc.plateforme_emploi.entity.Recruteur;
import com.odc.plateforme_emploi.exception.ResourceNotFoundException;
import com.odc.plateforme_emploi.exception.UnauthorizedOperationException;
import com.odc.plateforme_emploi.repository.OffreRepository;
import com.odc.plateforme_emploi.repository.RecruteurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OffreService {

    private final OffreRepository offreRepository;
    private final RecruteurRepository recruteurRepository;

    // Créer une offre
    public OffreResponse creerOffre(OffreRequest request, String emailRecruteur) {
        Recruteur recruteur = recruteurRepository.findByEmail(emailRecruteur)
            .orElseThrow(() -> new ResourceNotFoundException("Recruteur non trouvé !"));

        Offre offre = new Offre();
        offre.setTitre(request.getTitre());
        offre.setDescription(request.getDescription());
        offre.setLocalisation(request.getLocalisation());
        offre.setTypeContrat(request.getTypeContrat());
        offre.setNiveauEtude(request.getNiveauEtude());
        offre.setSalaire(request.getSalaire());
        offre.setDateExpiration(request.getDateExpiration());
        offre.setRecruteur(recruteur);

        return toResponse(offreRepository.save(offre));
    }

    // Toutes les offres actives
    public List<OffreResponse> getToutesLesOffres() {
        return offreRepository.findByStatut(Offre.StatutOffre.ACTIVE)
            .stream().map(this::toResponse)
            .collect(Collectors.toList());
    }

    // Une offre par ID
    public OffreResponse getOffreById(Long id) {
        Offre offre = offreRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée !"));
        return toResponse(offre);
    }

    // Offres d'un recruteur
    public List<OffreResponse> getOffresRecruteur(String emailRecruteur) {
        Recruteur recruteur = recruteurRepository.findByEmail(emailRecruteur)
            .orElseThrow(() -> new ResourceNotFoundException("Recruteur non trouvé !"));
        return offreRepository.findByRecruteurId(recruteur.getId())
            .stream().map(this::toResponse)
            .collect(Collectors.toList());
    }

    // Recherche par titre ou localisation
    public List<OffreResponse> rechercherOffres(String keyword) {
        return offreRepository
            .findByTitreContainingIgnoreCaseOrLocalisationContainingIgnoreCase(
                keyword, keyword)
            .stream().map(this::toResponse)
            .collect(Collectors.toList());
    }

    // Modifier une offre
    public OffreResponse modifierOffre(Long id, OffreRequest request,
                                        String emailRecruteur) {
        Offre offre = offreRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée !"));

        if (!offre.getRecruteur().getEmail().equals(emailRecruteur)) {
            throw new UnauthorizedOperationException("Vous n'êtes pas autorisé à modifier cette offre.");
        }

        offre.setTitre(request.getTitre());
        offre.setDescription(request.getDescription());
        offre.setLocalisation(request.getLocalisation());
        offre.setTypeContrat(request.getTypeContrat());
        offre.setNiveauEtude(request.getNiveauEtude());
        offre.setSalaire(request.getSalaire());
        offre.setDateExpiration(request.getDateExpiration());

        return toResponse(offreRepository.save(offre));
    }

    // Supprimer une offre
    public void supprimerOffre(Long id, String emailRecruteur) {
        Offre offre = offreRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée !"));

        if (!offre.getRecruteur().getEmail().equals(emailRecruteur)) {
            throw new UnauthorizedOperationException("Vous n'êtes pas autorisé à modifier cette offre.");
        }
        offreRepository.delete(offre);
    }

    // Convertir Offre → OffreResponse
    private OffreResponse toResponse(Offre offre) {
        OffreResponse response = new OffreResponse();
        response.setId(offre.getId());
        response.setTitre(offre.getTitre());
        response.setDescription(offre.getDescription());
        response.setLocalisation(offre.getLocalisation());
        response.setTypeContrat(offre.getTypeContrat());
        response.setNiveauEtude(offre.getNiveauEtude());
        response.setSalaire(offre.getSalaire());
        response.setStatut(offre.getStatut());
        response.setDatePublication(offre.getDatePublication());
        response.setDateExpiration(offre.getDateExpiration());
        response.setRecruteurId(offre.getRecruteur().getId());
        response.setEntreprise(offre.getRecruteur().getEntreprise());
        response.setSecteur(offre.getRecruteur().getSecteur());
        response.setNombreCandidatures(
            offre.getCandidatures() != null ? offre.getCandidatures().size() : 0
        );
        return response;
    }
}