
package models;

public enum TypeSalle {
    AMPHI,
    SALLE_TP,
    CLASSIQUE;

    @Override
    public String toString() {
        switch (this) {
            case AMPHI:      return "Amphithéâtre";
            case SALLE_TP:   return "Salle TP Informatique";
            case CLASSIQUE:  return "Salle Classique";
            default:         return name();
        }
    }
}