package com.odc.plateforme_emploi.service;

import com.odc.plateforme_emploi.dto.*;
import com.odc.plateforme_emploi.entity.*;
import com.odc.plateforme_emploi.exception.BadRequestException;
import com.odc.plateforme_emploi.exception.DuplicateResourceException;
import com.odc.plateforme_emploi.exception.ResourceNotFoundException;
import com.odc.plateforme_emploi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final CandidatRepository candidatRepository;
    private final RecruteurRepository recruteurRepository;
    private final PasswordEncoder passwordEncoder;

    // ============ ADMIN ============

    public List<UtilisateurResponse> getTousLesUtilisateurs() {
        return utilisateurRepository.findAll()
            .stream().map(this::toResponse)
            .collect(Collectors.toList());
    }

    public UtilisateurResponse getUtilisateurById(Long id) {
        Utilisateur u = utilisateurRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé !"));
        return toResponse(u);
    }

    // Admin crée un utilisateur (candidat ou recruteur)
    public UtilisateurResponse creerUtilisateur(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email déjà utilisé !");
        }
        String motDePasseEncode = passwordEncoder.encode(request.getMotDePasse());

        if (request.getRole() == Utilisateur.Role.CANDIDAT) {
            Candidat c = new Candidat();
            remplirChampsBase(c, request.getNom(), request.getPrenom(),
                request.getEmail(), motDePasseEncode, Utilisateur.Role.CANDIDAT);
            c.setTelephone(request.getTelephone());
            c.setCompetences(request.getCompetences());
            c.setBiographie(request.getBiographie());
            Candidat saved = candidatRepository.save(c);
            log.info("Utilisateur CANDIDAT créé par un admin : {}", saved.getEmail());
            return toResponse(saved);

        } else if (request.getRole() == Utilisateur.Role.RECRUTEUR) {
            Recruteur r = new Recruteur();
            remplirChampsBase(r, request.getNom(), request.getPrenom(),
                request.getEmail(), motDePasseEncode, Utilisateur.Role.RECRUTEUR);
            r.setEntreprise(request.getEntreprise());
            r.setSecteur(request.getSecteur());
            r.setTelephone(request.getTelephone());
            r.setSiteWeb(request.getSiteWeb());
            Recruteur saved = recruteurRepository.save(r);
            log.info("Utilisateur RECRUTEUR créé par un admin : {}", saved.getEmail());
            return toResponse(saved);

        } else if (request.getRole() == Utilisateur.Role.ADMIN) {
            // Seul un ADMIN déjà authentifié peut atteindre ce endpoint (@PreAuthorize
            // sur AdminController), donc créer un autre ADMIN ici est sûr.
            Candidat admin = new Candidat();
            remplirChampsBase(admin, request.getNom(), request.getPrenom(),
                request.getEmail(), motDePasseEncode, Utilisateur.Role.ADMIN);
            Candidat saved = candidatRepository.save(admin);
            log.warn("⚠️ Nouveau compte ADMIN créé : {}", saved.getEmail());
            return toResponse(saved);

        } else {
            throw new BadRequestException("Rôle invalide !");
        }
    }

    // Admin modifie n'importe quel utilisateur
    public UtilisateurResponse adminModifierUtilisateur(Long id, AdminUpdateUserRequest request) {
        Utilisateur u = utilisateurRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé !"));

        u.setNom(request.getNom());
        u.setPrenom(request.getPrenom());
        u.setEmail(request.getEmail());
        u.setActif(request.isActif());

        if (u instanceof Candidat candidat) {
            candidat.setTelephone(request.getTelephone());
            candidat.setCompetences(request.getCompetences());
        } else if (u instanceof Recruteur recruteur) {
            recruteur.setEntreprise(request.getEntreprise());
            recruteur.setSecteur(request.getSecteur());
        }

        log.info("Utilisateur #{} modifié par un admin", id);
        return toResponse(utilisateurRepository.save(u));
    }

    // Admin suspend / réactive un compte
    public UtilisateurResponse changerStatutActif(Long id, boolean actif, String emailAdminConnecte) {
        Utilisateur u = utilisateurRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé !"));

        if (!actif && u.getEmail().equals(emailAdminConnecte)) {
            throw new BadRequestException("Vous ne pouvez pas suspendre votre propre compte.");
        }

        u.setActif(actif);
        log.warn("Utilisateur #{} ({}) {} par {}", id, u.getEmail(),
            actif ? "réactivé" : "suspendu", emailAdminConnecte);
        return toResponse(utilisateurRepository.save(u));
    }

    // Admin supprime un utilisateur
    public void supprimerUtilisateur(Long id, String emailAdminConnecte) {
        Utilisateur u = utilisateurRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé !"));

        if (u.getEmail().equals(emailAdminConnecte)) {
            throw new BadRequestException("Vous ne pouvez pas supprimer votre propre compte.");
        }

        utilisateurRepository.delete(u);
        log.warn("Utilisateur #{} ({}) supprimé par {}", id, u.getEmail(), emailAdminConnecte);
    }

    // ============ UTILISATEUR (son propre profil) ============

    public UtilisateurResponse getMonProfil(String email) {
        Utilisateur u = utilisateurRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé !"));
        return toResponse(u);
    }

    public UtilisateurResponse modifierMonProfil(String email, UpdateProfilRequest request) {
        Utilisateur u = utilisateurRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé !"));

        // Vérifier que le nouvel email n'est pas déjà pris par un autre utilisateur
        if (!u.getEmail().equals(request.getEmail())
                && utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Cet email est déjà utilisé !");
        }

        u.setNom(request.getNom());
        u.setPrenom(request.getPrenom());
        u.setEmail(request.getEmail());

        if (request.getNouveauMotDePasse() != null && !request.getNouveauMotDePasse().isBlank()) {
            u.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
        }

        if (u instanceof Candidat candidat) {
            candidat.setTelephone(request.getTelephone());
            candidat.setCompetences(request.getCompetences());
            candidat.setBiographie(request.getBiographie());
        } else if (u instanceof Recruteur recruteur) {
            recruteur.setEntreprise(request.getEntreprise());
            recruteur.setSecteur(request.getSecteur());
            recruteur.setSiteWeb(request.getSiteWeb());
            recruteur.setTelephone(request.getTelephone());
        }

        return toResponse(utilisateurRepository.save(u));
    }

    // ============ HELPERS ============

    private void remplirChampsBase(Utilisateur u, String nom, String prenom,
                                    String email, String motDePasse, Utilisateur.Role role) {
        u.setNom(nom);
        u.setPrenom(prenom);
        u.setEmail(email);
        u.setMotDePasse(motDePasse);
        u.setRole(role);
        u.setActif(true);
        u.setEnabled(true); // créé par un admin → pas besoin de validation e-mail
    }

    private UtilisateurResponse toResponse(Utilisateur u) {
        UtilisateurResponse r = new UtilisateurResponse();
        r.setId(u.getId());
        r.setNom(u.getNom());
        r.setPrenom(u.getPrenom());
        r.setEmail(u.getEmail());
        r.setRole(u.getRole());
        r.setActif(u.isActif());
        r.setEnabled(u.isEnabled());

        if (u instanceof Candidat candidat) {
            r.setTelephone(candidat.getTelephone());
            r.setCompetences(candidat.getCompetences());
        } else if (u instanceof Recruteur recruteur) {
            r.setTelephone(recruteur.getTelephone());
            r.setEntreprise(recruteur.getEntreprise());
            r.setSecteur(recruteur.getSecteur());
        }

        return r;
    }
}
