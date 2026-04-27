
package models;

import java.util.Objects;

public class Salle {

    private int id;
    private String identifiantSalle;
    private int capaciteAccueil;
    private TypeSalle typeSalle;

   
    public Salle(String identifiantSalle, 
                 int capaciteAccueil, TypeSalle typeSalle) {
        this.identifiantSalle = Objects.requireNonNull(identifiantSalle, "identifiant requis");
        setCapaciteAccueil(capaciteAccueil);
        this.typeSalle = Objects.requireNonNull(typeSalle, "type salle requis");
    }

    
    public int getId() { return id; }
    public String getIdentifiantSalle() { return identifiantSalle; }
    public int getCapaciteAccueil() { return capaciteAccueil; }
    public TypeSalle getTypeSalle() { return typeSalle; }

    
    public void setId(int id) { this.id = id; }
    public void setIdentifiantSalle(String id) { 
        this.identifiantSalle = Objects.requireNonNull(id, "identifiant requis");
    }
    public void setCapaciteAccueil(int cap) { 
        if (cap <= 0) {
            throw new IllegalArgumentException("capacite accueil doit etre > 0");
        }
        this.capaciteAccueil = cap; 
    }
    public void setTypeSalle(TypeSalle type) { 
        this.typeSalle = Objects.requireNonNull(type, "type salle requis");
    }

    @Override
    public String toString() {
        return identifiantSalle + " (" + typeSalle + 
               " - " + capaciteAccueil + " places)";
    }
}