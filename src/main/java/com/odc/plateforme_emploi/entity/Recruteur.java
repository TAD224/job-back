package com.odc.plateforme_emploi.entity;



import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "recruteurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Recruteur extends Utilisateur {

    @Column(nullable = false)
    private String entreprise;

    private String secteur;

    private String telephone;

    @Column(name = "site_web")
    private String siteWeb;

    @OneToMany(mappedBy = "recruteur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Offre> offres;
}
