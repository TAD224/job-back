package com.odc.plateforme_emploi.entity;



import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "candidats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Candidat extends Utilisateur {

    @Column(name = "cv_path")
    private String cvPath;         // chemin du fichier CV uploadé

    @Column(columnDefinition = "TEXT")
    private String competences;

    @Column(columnDefinition = "TEXT")
    private String biographie;

    @Column(name = "telephone")
    private String telephone;

    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Candidature> candidatures;
}
