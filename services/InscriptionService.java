package services;

import dao.InscriptionDao;
import models.Etudiant;
import models.Groupe;
import models.Inscription;

import java.time.LocalDate;

public class InscriptionService {

    private final InscriptionDao inscriptionDao;

    public InscriptionService() {
        this.inscriptionDao = new InscriptionDao();
    }

    public InscriptionService(InscriptionDao inscriptionDao) {
        this.inscriptionDao = inscriptionDao;
    }

    public Inscription inscrireEtudiant(Etudiant etudiant, Groupe groupe) {
        if (etudiant == null || etudiant.getId() <= 0) {
            throw new IllegalArgumentException("L'etudiant doit etre renseigne et persiste");
        }
        if (groupe == null || groupe.getId() <= 0) {
            throw new IllegalArgumentException("Le groupe doit etre renseigne et persiste");
        }

        inscriptionDao.findByEtudiantAndGroupe(etudiant.getId(), groupe.getId())
                .ifPresent(i -> {
                    throw new IllegalStateException("Cet etudiant est deja inscrit dans ce groupe");
                });

        int effectifActuel = inscriptionDao.countByGroupe(groupe.getId());
        if (effectifActuel >= groupe.getCapacite()) {
            throw new IllegalStateException("Le groupe est plein");
        }

        Inscription inscription = new Inscription(etudiant, groupe, LocalDate.now());
        return inscriptionDao.save(inscription);
    }

    public void enregistrerNote(int inscriptionId, float note) {
        Inscription inscription = inscriptionDao.findById(inscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscription introuvable"));
        inscription.setNote(note);
        inscriptionDao.update(inscription);
    }

    public void calculerEtSauverMoyenne(int inscriptionId, float[] notes, float[] coefficients) {
        Inscription inscription = inscriptionDao.findById(inscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscription introuvable"));
        inscription.calculMoyenne(notes, coefficients);
        inscriptionDao.update(inscription);
    }
}
