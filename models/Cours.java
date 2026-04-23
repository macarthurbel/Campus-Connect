
package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Cours {

    private int id;
    private String code;
    private String intitule;
    private String description;
    private int volumeHoraire;
    private Enseignant enseignantResponsable;
    private List<Groupe> groupes;

    // Constructeur
    public Cours(String code, String intitule, 
                 String description, int volumeHoraire,
                 Enseignant enseignantResponsable) {
        this.code = Objects.requireNonNull(code, "code requis");
        this.intitule = Objects.requireNonNull(intitule, "intitule requis");
        this.description = Objects.requireNonNull(description, "description requise");
        setVolumeHoraire(volumeHoraire);
        this.enseignantResponsable = enseignantResponsable;
        this.groupes = new ArrayList<>();
    }

    // Getters
    public int getId() { return id; }
    public String getCode() { return code; }
    public String getIntitule() { return intitule; }
    public String getDescription() { return description; }
    public int getVolumeHoraire() { return volumeHoraire; }
    public Enseignant getEnseignantResponsable() { 
        return enseignantResponsable; 
    }
    public List<Groupe> getGroupes() { return Collections.unmodifiableList(groupes); }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setCode(String code) { this.code = Objects.requireNonNull(code, "code requis"); }
    public void setIntitule(String intitule) { 
        this.intitule = Objects.requireNonNull(intitule, "intitule requis");
    }
    public void setDescription(String desc) { 
        this.description = Objects.requireNonNull(desc, "description requise");
    }
    public void setVolumeHoraire(int vh) { 
        if (vh <= 0) {
            throw new IllegalArgumentException("volume horaire doit etre > 0");
        }
        this.volumeHoraire = vh; 
    }
    public void setEnseignantResponsable(Enseignant e) { 
        this.enseignantResponsable = e; 
    }

    
    public void ajouterGroupe(Groupe groupe) {
        if (groupe != null && !this.groupes.contains(groupe)) {
            this.groupes.add(groupe);
        }
    }

    @Override
    public String toString() {
        return code + " - " + intitule;
    }
}