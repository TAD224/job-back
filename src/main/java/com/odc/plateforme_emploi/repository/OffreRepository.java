package com.odc.plateforme_emploi.repository;

import com.odc.plateforme_emploi.entity.Offre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OffreRepository extends JpaRepository<Offre, Long> {

    // Toutes les offres d'un recruteur
    List<Offre> findByRecruteurId(Long recruteurId);

    // Offres actives uniquement
    List<Offre> findByStatut(Offre.StatutOffre statut);

    // Recherche par titre ou localisation
    List<Offre> findByTitreContainingIgnoreCaseOrLocalisationContainingIgnoreCase(
        String titre, String localisation
    );

    // Offres par type de contrat
    List<Offre> findByTypeContrat(String typeContrat);
}
