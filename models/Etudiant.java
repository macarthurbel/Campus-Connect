
package models;

import java.time.LocalDate;
import java.util.Objects;

public class Etudiant extends Personne {

    private int id;
    private String matricule;
    private String filiere;
    private int annee;

   
    public Etudiant(String nom, String prenom, String email,
                    LocalDate dateNaissance, String matricule,
                    String filiere, int annee) {
        super(nom, prenom, email, dateNaissance);
        this.matricule = Objects.requireNonNull(matricule, "matricule requis");
        this.filiere = Objects.requireNonNull(filiere, "filiere requise");
        setAnnee(annee);
    }

    public Etudiant(String nom, String prenom, String email,
                    String dateNaissance, String matricule,
                    String filiere, int annee) {
        this(nom, prenom, email, LocalDate.parse(dateNaissance), matricule, filiere, annee);
    }

    
    public int getId() { return id; }
    public String getMatricule() { return matricule; }
    public String getFiliere() { return filiere; }
    public int getAnnee() { return annee; }

    
    public void setId(int id) { this.id = id; }
    public void setMatricule(String matricule) { 
        this.matricule = Objects.requireNonNull(matricule, "matricule requis");
    }
    public void setFiliere(String filiere) { 
        this.filiere = Objects.requireNonNull(filiere, "filiere requise");
    }
    public void setAnnee(int annee) {
        if (annee < 1) {
            throw new IllegalArgumentException("annee doit etre >= 1");
        }
        this.annee = annee;
    }

    
    @Override
    public String afficherDetails() {
        return "Etudiant: " + getPrenom() + " " + getNom() +
               " | Matricule: " + matricule +
               " | Filière: " + filiere +
               " | Année: L" + annee;
    }
}
