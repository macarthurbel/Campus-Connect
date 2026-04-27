
package models;

import java.time.LocalDate;
import java.util.Objects;

public class Enseignant extends Personne {

    private int id;
    private String departement;
    private StatutEnseignant statut;

    
    public Enseignant(String nom, String prenom, String email,
                      LocalDate dateNaissance, String departement,
                      StatutEnseignant statut) {
        super(nom, prenom, email, dateNaissance);
        this.departement = Objects.requireNonNull(departement, "departement requis");
        this.statut = Objects.requireNonNull(statut, "statut requis");
    }

    public Enseignant(String nom, String prenom, String email,
                      String dateNaissance, String departement,
                      StatutEnseignant statut) {
        this(nom, prenom, email, LocalDate.parse(dateNaissance), departement, statut);
    }

    
    public int getId() { return id; }
    public String getDepartement() { return departement; }
    public StatutEnseignant getStatut() { return statut; }

    
    public void setId(int id) { this.id = id; }
    public void setDepartement(String departement) { 
        this.departement = Objects.requireNonNull(departement, "departement requis");
    }
    public void setStatut(StatutEnseignant statut) {
        this.statut = Objects.requireNonNull(statut, "statut requis");
    }

    
    @Override
    public String afficherDetails() {
        return "Enseignant: " + getPrenom() + " " + getNom() +
               " | Département: " + departement +
               " | Statut: " + statut;
    }
}