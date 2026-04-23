
package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Groupe {

    private int id;
    private String nom;
    private int capacite;
    private Enseignant enseignant;
    private TypeGroupe type;
    private List<Etudiant> listeEtudiants;

  
    public Groupe(String nom, int capacite, Enseignant enseignant, TypeGroupe type) {
        this.nom = Objects.requireNonNull(nom, "nom requis");
        this.listeEtudiants = new ArrayList<>();
        setCapacite(capacite);
        this.enseignant = enseignant;
        this.type = Objects.requireNonNull(type, "type groupe requis");
    }

    
    public int getId() { return id; }
    public String getNom() { return nom; }
    public int getCapacite() { return capacite; }
    public Enseignant getEnseignant() { return enseignant; }
    public TypeGroupe getType() { return type; }
    public List<Etudiant> getListeEtudiants() { 
        return Collections.unmodifiableList(listeEtudiants);
    }

    
    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = Objects.requireNonNull(nom, "nom requis"); }
    public void setCapacite(int capacite) { 
        if (capacite <= 0) {
            throw new IllegalArgumentException("capacite doit etre > 0");
        }
        if (capacite < listeEtudiants.size()) {
            throw new IllegalArgumentException("capacite inferieure au nombre d'etudiants deja inscrits");
        }
        this.capacite = capacite; 
    }
    public void setEnseignant(Enseignant e) { 
        this.enseignant = e; 
    }
    public void setType(TypeGroupe type) {
        this.type = Objects.requireNonNull(type, "type groupe requis");
    }

    
    public boolean ajouterEtudiant(Etudiant etudiant) {
        if (etudiant == null || listeEtudiants.contains(etudiant)) {
            return false;
        }
        if (listeEtudiants.size() >= capacite) {
            return false; 
        }
        listeEtudiants.add(etudiant);
        return true;
    }

    
    public boolean estPlein() {
        return listeEtudiants.size() >= capacite;
    }

    
    public int placesRestantes() {
        return capacite - listeEtudiants.size();
    }

    @Override
    public String toString() {
        return nom + " (" + listeEtudiants.size() + 
               "/" + capacite + ")";
    }
}