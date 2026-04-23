
package models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class Seance {

    private int id;
    private String idSeance;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Groupe groupe;
    private Enseignant enseignant;
    private Salle salle;
    private Cours cours;

    
    public Seance(String idSeance, LocalDate date,
                  LocalTime heureDebut, LocalTime heureFin,
                  Groupe groupe, Enseignant enseignant,
                  Salle salle, Cours cours) {
        this.idSeance = idSeance;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.groupe = groupe;
        this.enseignant = enseignant;
        this.salle = salle;
        this.cours = cours;
        validerHoraire();
    }

    public Seance(String idSeance, String date,
                  String heureDebut, String heureFin,
                  Groupe groupe, Enseignant enseignant,
                  Salle salle, Cours cours) {
        this(idSeance, LocalDate.parse(date), LocalTime.parse(heureDebut), LocalTime.parse(heureFin),
                groupe, enseignant, salle, cours);
    }

    
    public int getId() { return id; }
    public String getIdSeance() { return idSeance; }
    public LocalDate getDate() { return date; }
    public String getDateIso() { return date.toString(); }
    public LocalTime getHeureDebut() { return heureDebut; }
    public LocalTime getHeureFin() { return heureFin; }
    public String getHeureDebutIso() { return heureDebut.toString(); }
    public String getHeureFinIso() { return heureFin.toString(); }
    public Groupe getGroupe() { return groupe; }
    public Enseignant getEnseignant() { return enseignant; }
    public Salle getSalle() { return salle; }
    public Cours getCours() { return cours; }

   
    public void setId(int id) { this.id = id; }
    public void setIdSeance(String id) { this.idSeance = id; }
    public void setDate(LocalDate date) { this.date = Objects.requireNonNull(date, "date requise"); }
    public void setDate(String date) { this.date = LocalDate.parse(date); }
    public void setHeureDebut(LocalTime h) {
        this.heureDebut = Objects.requireNonNull(h, "heure debut requise");
        validerHoraire();
    }
    public void setHeureDebut(String h) { setHeureDebut(LocalTime.parse(h)); }
    public void setHeureFin(LocalTime h) {
        this.heureFin = Objects.requireNonNull(h, "heure fin requise");
        validerHoraire();
    }
    public void setHeureFin(String h) { setHeureFin(LocalTime.parse(h)); }
    public void setGroupe(Groupe g) { this.groupe = g; }
    public void setEnseignant(Enseignant e) { 
        this.enseignant = e; 
    }
    public void setSalle(Salle s) { this.salle = s; }
    public void setCours(Cours cours) { this.cours = cours; }

    
    public boolean chevauche(Seance autre) {
        if (!this.date.equals(autre.date)) {
            return false;
        }
        return this.heureDebut.isBefore(autre.heureFin) &&
               this.heureFin.isAfter(autre.heureDebut);
    }

    private void validerHoraire() {
        if (heureDebut != null && heureFin != null && !heureDebut.isBefore(heureFin)) {
            throw new IllegalArgumentException("heure debut doit etre strictement avant heure fin");
        }
    }

    @Override
    public String toString() {
        return idSeance + " | " + date + 
               " " + heureDebut + "-" + heureFin +
               " | " + salle;
    }
}