package com.odc.plateforme_emploi.security;

import com.odc.plateforme_emploi.entity.Utilisateur;
import com.odc.plateforme_emploi.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Utilisateur non trouvé avec l'email : " + email
                ));

        return User.builder()
            .username(utilisateur.getEmail())
            .password(utilisateur.getMotDePasse())
            .authorities(List.of(
                new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name())
            ))
            // ✅ Le compte doit être actif (non suspendu) ET activé (e-mail confirmé).
            // Vérification déjà faite explicitement dans AuthService.login() avec des
            // messages distincts ; ceci est une double sécurité au niveau Spring Security.
            .disabled(!(utilisateur.isActif() && utilisateur.isEnabled()))
            .build();
    }
}