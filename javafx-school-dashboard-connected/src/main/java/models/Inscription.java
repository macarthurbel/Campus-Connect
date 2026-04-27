
package models;

import java.time.LocalDate;
import java.util.Objects;

public class Inscription {

    private int id;
    private Etudiant etudiant;
    private Groupe groupe;
    private LocalDate dateInscription;
    private float moyenne;
    private float note;

    
    public Inscription(Etudiant etudiant, Groupe groupe,
                       LocalDate dateInscription) {
        this.etudiant = Objects.requireNonNull(etudiant, "etudiant requis");
        this.groupe = Objects.requireNonNull(groupe, "groupe requis");
        this.dateInscription = dateInscription;
        this.moyenne = 0.0f;
        this.note = 0.0f;
    }

    public Inscription(Etudiant etudiant, Groupe groupe,
                       String dateInscription) {
        this(etudiant, groupe, LocalDate.parse(dateInscription));
    }

    
    public int getId() { return id; }
    public Etudiant getEtudiant() { return etudiant; }
    public Groupe getGroupe() { return groupe; }
    public LocalDate getDateInscription() { return dateInscription; }
    public String getDateInscriptionIso() { return dateInscription.toString(); }
    public float getMoyenne() { return moyenne; }
    public float getNote() { return note; }

    
    public void setId(int id) { this.id = id; }
    
    public void setMoyenne(float moyenne) {
        if (moyenne < 0) {
            this.moyenne = 0;
        } else {
            this.moyenne = moyenne;
        }
    }
    public void setNote(float note) {
        
        if (note < 0) this.note = 0;
        else if (note > 20) this.note = 20;
        else this.note = note;
    }

    
    public float calculMoyenne(float[] notes, float[] coefficients) {
        if (notes == null || coefficients == null || notes.length == 0 || notes.length != coefficients.length) {
            throw new IllegalArgumentException("notes et coefficients doivent avoir la meme taille non nulle");
        }
        float somme = 0;
        float totalCoeff = 0;
        for (int i = 0; i < notes.length; i++) {
            if (notes[i] < 0 || notes[i] > 20) {
                throw new IllegalArgumentException("chaque note doit etre entre 0 et 20");
            }
            if (coefficients[i] <= 0) {
                throw new IllegalArgumentException("chaque coefficient doit etre > 0");
            }
            somme += notes[i] * coefficients[i];
            totalCoeff += coefficients[i];
        }
        this.moyenne = totalCoeff > 0 ? somme / totalCoeff : 0;
        return this.moyenne;
    }

    @Override
    public String toString() {
        return etudiant + " → " + groupe + 
               " | Moyenne: " + moyenne;
    }
}