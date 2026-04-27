package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public abstract class Personne {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private String nom;
    private String prenom;
    private String email;
    private LocalDate dateNaissance;

    // Constructeur
    public Personne(String nom, String prenom,
                    String email, LocalDate dateNaissance) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.dateNaissance = dateNaissance;
    }

    public Personne(String nom, String prenom,
                    String email, String dateNaissance) {
        this(nom, prenom, email, LocalDate.parse(dateNaissance, DATE_FORMAT));
    }

    
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public String getDateNaissanceIso() { return dateNaissance.format(DATE_FORMAT); }

    
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setEmail(String email) { this.email = email; }
    public void setDateNaissance(LocalDate date) {
        this.dateNaissance = Objects.requireNonNull(date, "dateNaissance ne peut pas etre null");
    }

    public void setDateNaissance(String date) {
        this.dateNaissance = LocalDate.parse(date, DATE_FORMAT);
    }

    
    public abstract String afficherDetails();

    @Override
    public String toString() {
        return prenom + " " + nom;
    }
}
