package services;

import dao.SeanceDao;
import models.Seance;

import java.util.List;

public class PlanificationService {

    private final SeanceDao seanceDao;

    public PlanificationService() {
        this.seanceDao = new SeanceDao();
    }

    public PlanificationService(SeanceDao seanceDao) {
        this.seanceDao = seanceDao;
    }

    public Seance planifier(Seance nouvelleSeance) {
        if (nouvelleSeance == null) {
            throw new IllegalArgumentException("La seance ne peut pas etre null");
        }
        if (nouvelleSeance.getGroupe() == null || nouvelleSeance.getGroupe().getId() <= 0) {
            throw new IllegalArgumentException("Le groupe de la seance doit etre persiste");
        }
        if (nouvelleSeance.getEnseignant() == null || nouvelleSeance.getEnseignant().getId() <= 0) {
            throw new IllegalArgumentException("L'enseignant de la seance doit etre persiste");
        }
        if (nouvelleSeance.getSalle() == null || nouvelleSeance.getSalle().getId() <= 0) {
            throw new IllegalArgumentException("La salle de la seance doit etre persistee");
        }
        if (nouvelleSeance.getCours() == null || nouvelleSeance.getCours().getId() <= 0) {
            throw new IllegalArgumentException("Le cours de la seance doit etre persiste");
        }

        List<Seance> seancesDuJour = seanceDao.findByDate(nouvelleSeance.getDateIso());
        for (Seance existante : seancesDuJour) {
            if (!existante.chevauche(nouvelleSeance)) {
                continue;
            }

            if (existante.getSalle().getId() == nouvelleSeance.getSalle().getId()) {
                throw new IllegalStateException("Conflit: la salle est deja occupee sur ce creneau");
            }
            if (existante.getEnseignant().getId() == nouvelleSeance.getEnseignant().getId()) {
                throw new IllegalStateException("Conflit: l'enseignant a deja une seance sur ce creneau");
            }
            if (existante.getGroupe().getId() == nouvelleSeance.getGroupe().getId()) {
                throw new IllegalStateException("Conflit: le groupe a deja une seance sur ce creneau");
            }
        }

        return seanceDao.save(nouvelleSeance);
    }
}
