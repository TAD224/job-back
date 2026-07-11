package com.odc.plateforme_emploi.repository;



import com.odc.plateforme_emploi.entity.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

    // Toutes les candidatures d'un candidat
    List<Candidature> findByCandidatId(Long candidatId);

    // Toutes les candidatures pour une offre
    List<Candidature> findByOffreId(Long offreId);

    // Vérifier si un candidat a déjà postulé à une offre
    boolean existsByCandidatIdAndOffreId(Long candidatId, Long offreId);

    // Candidatures par statut
    List<Candidature> findByStatut(Candidature.StatutCandidature statut);
}
