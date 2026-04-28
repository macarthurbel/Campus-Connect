package models;

public class Note {
    private int id;
    private int etudiantId;
    private int coursId;
    private double score;
    private double coefficient;
    private String dateCreation;

    public Note(int etudiantId, int coursId, double score, double coefficient, String dateCreation) {
        this.etudiantId = etudiantId;
        this.coursId = coursId;
        this.score = score;
        this.coefficient = coefficient;
        this.dateCreation = dateCreation;
    }

    public Note(int id, int etudiantId, int coursId, double score, double coefficient, String dateCreation) {
        this.id = id;
        this.etudiantId = etudiantId;
        this.coursId = coursId;
        this.score = score;
        this.coefficient = coefficient;
        this.dateCreation = dateCreation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEtudiantId() {
        return etudiantId;
    }

    public void setEtudiantId(int etudiantId) {
        this.etudiantId = etudiantId;
    }

    public int getCoursId() {
        return coursId;
    }

    public void setCoursId(int coursId) {
        this.coursId = coursId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(double coefficient) {
        this.coefficient = coefficient;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }
}
