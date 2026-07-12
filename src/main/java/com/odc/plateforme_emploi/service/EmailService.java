package com.odc.plateforme_emploi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Envoi d'e-mails transactionnels via l'API HTTP de Brevo (https://api.brevo.com/v3/smtp/email)
 * plutôt que via SMTP classique.
 *
 * Pourquoi : les hébergeurs cloud (Render inclus, sur son offre gratuite) bloquent le trafic
 * sortant sur les ports SMTP (25, 465, 587) pour lutter contre le spam. L'API HTTP de Brevo
 * passe par le port 443 (HTTPS), jamais bloqué, ce qui rend l'envoi de mails fiable même sur
 * les plans gratuits.
 *
 * ⚠️ Nécessite une clé API Brevo v3 (Paramètres → SMTP & API → Clés API et MCP → Générer une
 * nouvelle clé API), différente de la clé SMTP utilisée précédemment.
 */
@Slf4j
@Service
public class EmailService {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.mail.brevo.api-key}")
    private String brevoApiKey;

    public void envoyerEmailActivation(String destinataire, String prenom, String token) {
        String lienActivation = frontendUrl + "/activation?token=" + token;
        String sujet = "Activez votre compte JobPlatform.GN";
        String contenuHtml = construireTemplateActivation(prenom, lienActivation);

        envoyer(destinataire, sujet, contenuHtml);
    }

    public void envoyerNotificationNouvelleCandidature(String destinataireRecruteur,
                                                         String prenomRecruteur,
                                                         String nomCompletCandidat,
                                                         String titreOffre) {
        String lienDashboard = frontendUrl + "/recruteur/offres";
        String sujet = "Nouvelle candidature reçue — " + titreOffre;
        String contenuHtml = construireTemplateNouvelleCandidature(
            prenomRecruteur, nomCompletCandidat, titreOffre, lienDashboard
        );

        envoyer(destinataireRecruteur, sujet, contenuHtml);
    }

    public void envoyerNotificationChangementStatut(String destinataireCandidat,
                                                      String prenomCandidat,
                                                      String titreOffre,
                                                      String entreprise,
                                                      boolean accepte) {
        String lienCandidatures = frontendUrl + "/mes-candidatures";
        String sujet = accepte
            ? "Bonne nouvelle pour votre candidature — " + titreOffre
            : "Réponse à votre candidature — " + titreOffre;
        String contenuHtml = construireTemplateChangementStatut(
            prenomCandidat, titreOffre, entreprise, accepte, lienCandidatures
        );

        envoyer(destinataireCandidat, sujet, contenuHtml);
    }

    private void envoyer(String destinataire, String sujet, String contenuHtml) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);
        headers.set("accept", "application/json");

        Map<String, Object> corps = Map.of(
            "sender", Map.of("name", fromName, "email", fromAddress),
            "to", List.of(Map.of("email", destinataire)),
            "subject", sujet,
            "htmlContent", contenuHtml
        );

        try {
            HttpEntity<Map<String, Object>> requete = new HttpEntity<>(corps, headers);
            restTemplate.postForEntity(BREVO_API_URL, requete, String.class);
            log.info("E-mail envoyé à {} : {}", destinataire, sujet);
        } catch (RestClientException e) {
            log.error("Échec de l'envoi de l'e-mail à {} via l'API Brevo", destinataire, e);
            throw new RuntimeException("Impossible d'envoyer l'e-mail. Merci de réessayer plus tard.");
        }
    }

    private String construireTemplateActivation(String prenom, String lienActivation) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0; padding:0; background-color:#f4f6f8; font-family:Arial, Helvetica, sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f8; padding:32px 0;">
                <tr>
                  <td align="center">
                    <table width="480" cellpadding="0" cellspacing="0" style="background-color:#ffffff; border-radius:8px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.06);">
                      <tr>
                        <td style="background-color:#0f766e; padding:24px 32px;">
                          <span style="color:#ffffff; font-size:20px; font-weight:bold;">JobPlatform.GN</span>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:32px;">
                          <h2 style="color:#111827; margin-top:0;">Bienvenue %s !</h2>
                          <p style="color:#374151; font-size:15px; line-height:1.6;">
                            Merci de vous être inscrit sur JobPlatform.GN. Pour activer votre compte
                            et commencer à utiliser la plateforme, cliquez sur le bouton ci-dessous :
                          </p>
                          <table cellpadding="0" cellspacing="0" style="margin:24px 0;">
                            <tr>
                              <td style="border-radius:6px; background-color:#0f766e;">
                                <a href="%s" style="display:inline-block; padding:12px 28px; color:#ffffff; text-decoration:none; font-weight:bold; font-size:15px;">
                                  Activer mon compte
                                </a>
                              </td>
                            </tr>
                          </table>
                          <p style="color:#6b7280; font-size:13px; line-height:1.6;">
                            Ce lien est valable 24 heures. Si le bouton ne fonctionne pas, copiez ce lien dans votre navigateur :<br>
                            <a href="%s" style="color:#0f766e; word-break:break-all;">%s</a>
                          </p>
                          <p style="color:#9ca3af; font-size:12px; margin-top:32px;">
                            Si vous n'êtes pas à l'origine de cette inscription, ignorez simplement cet e-mail.
                          </p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(prenom, lienActivation, lienActivation, lienActivation);
    }

    private String construireTemplateNouvelleCandidature(String prenomRecruteur,
                                                           String nomCompletCandidat,
                                                           String titreOffre,
                                                           String lienDashboard) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0; padding:0; background-color:#f4f6f8; font-family:Arial, Helvetica, sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f8; padding:32px 0;">
                <tr>
                  <td align="center">
                    <table width="480" cellpadding="0" cellspacing="0" style="background-color:#ffffff; border-radius:8px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.06);">
                      <tr>
                        <td style="background-color:#0f766e; padding:24px 32px;">
                          <span style="color:#ffffff; font-size:20px; font-weight:bold;">JobPlatform.GN</span>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:32px;">
                          <h2 style="color:#111827; margin-top:0;">Nouvelle candidature reçue</h2>
                          <p style="color:#374151; font-size:15px; line-height:1.6;">
                            Bonjour %s,<br><br>
                            <strong>%s</strong> vient de postuler à votre offre
                            <strong>« %s »</strong>.
                          </p>
                          <table cellpadding="0" cellspacing="0" style="margin:24px 0;">
                            <tr>
                              <td style="border-radius:6px; background-color:#0f766e;">
                                <a href="%s" style="display:inline-block; padding:12px 28px; color:#ffffff; text-decoration:none; font-weight:bold; font-size:15px;">
                                  Voir la candidature
                                </a>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(prenomRecruteur, nomCompletCandidat, titreOffre, lienDashboard);
    }

    private String construireTemplateChangementStatut(String prenomCandidat,
                                                        String titreOffre,
                                                        String entreprise,
                                                        boolean accepte,
                                                        String lienCandidatures) {
        String couleur = accepte ? "#057642" : "#0f766e";
        String titre = accepte ? "Votre candidature a été retenue 🎉" : "Mise à jour de votre candidature";
        String corps = accepte
            ? "Nous avons le plaisir de vous informer que votre candidature au poste <strong>« " + titreOffre
              + " »</strong> chez <strong>" + entreprise + "</strong> a été retenue par le recruteur. "
              + "Celui-ci reviendra vers vous prochainement pour la suite du processus."
            : "Nous vous remercions pour l'intérêt porté au poste <strong>« " + titreOffre
              + " »</strong> chez <strong>" + entreprise + "</strong>. Après étude de votre profil, "
              + "le recruteur a choisi de ne pas donner suite à votre candidature pour ce poste. "
              + "N'hésitez pas à consulter nos autres offres.";

        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0; padding:0; background-color:#f4f6f8; font-family:Arial, Helvetica, sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f8; padding:32px 0;">
                <tr>
                  <td align="center">
                    <table width="480" cellpadding="0" cellspacing="0" style="background-color:#ffffff; border-radius:8px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.06);">
                      <tr>
                        <td style="background-color:%s; padding:24px 32px;">
                          <span style="color:#ffffff; font-size:20px; font-weight:bold;">JobPlatform.GN</span>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:32px;">
                          <h2 style="color:#111827; margin-top:0;">%s</h2>
                          <p style="color:#374151; font-size:15px; line-height:1.6;">
                            Bonjour %s,<br><br>%s
                          </p>
                          <table cellpadding="0" cellspacing="0" style="margin:24px 0;">
                            <tr>
                              <td style="border-radius:6px; background-color:%s;">
                                <a href="%s" style="display:inline-block; padding:12px 28px; color:#ffffff; text-decoration:none; font-weight:bold; font-size:15px;">
                                  Voir mes candidatures
                                </a>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(couleur, titre, prenomCandidat, corps, couleur, lienCandidatures);
    }
}