package com.odc.plateforme_emploi.config;

import com.odc.plateforme_emploi.entity.Candidat;
import com.odc.plateforme_emploi.entity.Utilisateur;
import com.odc.plateforme_emploi.repository.CandidatRepository;
import com.odc.plateforme_emploi.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crée automatiquement le tout premier compte ADMIN au démarrage de l'application,
 * s'il n'en existe encore aucun. Nécessaire car tous les endpoints de création
 * d'utilisateurs (y compris ADMIN) exigent déjà d'être authentifié en tant qu'ADMIN —
 * sans ce bootstrap, personne ne pourrait jamais créer le premier compte admin.
 *
 * Configurable via variables d'environnement :
 *   ADMIN_BOOTSTRAP_EMAIL    (défaut : admin@jobplatform.gn)
 *   ADMIN_BOOTSTRAP_PASSWORD (défaut : ChangeMoi123! — À CHANGER en production)
 *
 * Ne fait rien si un compte ADMIN existe déjà (relances suivantes sans effet).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final CandidatRepository candidatRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.bootstrap.email:admin@jobplatform.gn}")
    private String adminEmail;

    @Value("${app.admin.bootstrap.password:ChangeMoi123!}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        boolean adminExiste = !utilisateurRepository.findByRole(Utilisateur.Role.ADMIN).isEmpty();
        if (adminExiste) {
            return;
        }

        Candidat admin = new Candidat();
        admin.setNom("Administrateur");
        admin.setPrenom("Principal");
        admin.setEmail(adminEmail);
        admin.setMotDePasse(passwordEncoder.encode(adminPassword));
        admin.setRole(Utilisateur.Role.ADMIN);
        admin.setActif(true);
        admin.setEnabled(true); // pas besoin d'activation par e-mail pour ce compte

        candidatRepository.save(admin);

        log.warn("========================================================");
        log.warn("⚠️  Compte ADMIN créé automatiquement au premier démarrage :");
        log.warn("    email    : {}", adminEmail);
        log.warn("    mot de passe : celui défini par ADMIN_BOOTSTRAP_PASSWORD");
        log.warn("    Pense à changer ce mot de passe une fois connecté !");
        log.warn("========================================================");
    }
}