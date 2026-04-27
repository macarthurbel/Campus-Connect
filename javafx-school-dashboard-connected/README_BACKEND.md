# Backend Java MVC - Corrections et ameliorations (Iteration 1)

## Objectif de cette iteration
Cette iteration se concentre uniquement sur la logique backend (modele metier, persistance SQLite, services metier), en laissant la couche visuelle JavaFX pour la suite.

## Ce qui a ete corrige

### 1) Modeles metier renforces
Les classes du package `models` ont ete corrigees et ameliorees selon le diagramme UML fourni :

- `Personne`
  - Passage de `dateNaissance` en `LocalDate` (au lieu de `String`).
  - Surcharge de constructeur pour accepter aussi une date ISO (`yyyy-MM-dd`).

- `Etudiant`
  - Validation de `matricule`, `filiere`, `annee`.
  - Constructeurs compatibles `LocalDate` et `String`.

- `Enseignant`
  - Remplacement du `statut` texte par l'enum `StatutEnseignant`.
  - Validations sur les champs obligatoires.

- `Groupe`
  - Ajout du type de groupe UML via enum `TypeGroupe` (`CM`, `TD`, `TP`).
  - Validation de capacite.
  - Protection contre les doublons d'etudiants.
  - Liste des etudiants exposee en lecture seule (`unmodifiableList`).

- `Cours`
  - Validation des attributs obligatoires.
  - Protection contre ajout de groupe en doublon.
  - Validation du volume horaire.

- `Inscription`
  - `dateInscription` en `LocalDate`.
  - Calcul de moyenne securise (tailles de tableaux, bornes des notes, coefficients > 0).

- `Salle`
  - Validation identifiant/capacite/type.

- `Seance`
  - `date` en `LocalDate`, `heureDebut`/`heureFin` en `LocalTime`.
  - Validation horaire (`debut < fin`).
  - Ajout de la relation `Cours`.
  - Detection de chevauchement plus fiable.

- Nouveaux enums
  - `TypeGroupe`
  - `StatutEnseignant`

### 2) Infrastructure SQLite ajoutee

- Nouveau fichier `utils/DatabaseManager.java`
  - Connexion SQLite centralisee.
  - Activation des cles etrangeres (`PRAGMA foreign_keys = ON`).
  - Initialisation automatique du schema (`CREATE TABLE IF NOT EXISTS`).
  - Dossier base cree automatiquement : `src/ressources/database`.

Tables creees :
- `enseignants`
- `etudiants`
- `groupes`
- `cours`
- `salles`
- `seances`
- `inscriptions`

### 3) Couche DAO (persistance) ajoutee

Package `dao` ajoute avec CRUD principal :
- `EtudiantDao`
- `EnseignantDao`
- `GroupeDao`
- `SalleDao`
- `CoursDao`
- `SeanceDao`
- `InscriptionDao`

Points importants :
- Utilisation de `PreparedStatement` (evite SQL injection).
- Mapping objet <-> table pour chaque entite.
- Recuperation des IDs auto-generes.
- Methode `countByGroupe` dans `InscriptionDao` pour gerer la capacite.

### 4) Services metier ajoutes

Package `services` :
- `InscriptionService`
  - Verifie qu'un etudiant n'est pas deja inscrit dans le groupe.
  - Verifie la capacite disponible avant inscription.
  - Permet de saisir note et moyenne.

- `PlanificationService`
  - Controle les conflits de seances (salle, enseignant, groupe) sur un creneau.

- `ApplicationBootstrapService`
  - Initialise la base de donnees au demarrage.

### 5) Point d'entree

- `Main.java`
  - Suppression de la reference directe au controleur `ageController` qui cassait la compilation.
  - Initialisation du backend SQLite au lancement.

## Validation technique effectuee
Compilation backend executee avec succes sur les packages :
- `models`
- `dao`
- `services`
- `utils`

## Notes importantes
- Le PDF `projet_java.pdf` n'a pas pu etre extrait automatiquement dans cet environnement (outil `pdftotext` absent).
- Les choix metier ont ete alignes sur le diagramme UML fourni et la structure du projet.
- La couche JavaFX (FXML/controllers complets) est volontairement reportee a la prochaine iteration.

## Prochaine iteration recommandee
1. Ajouter les controllers MVC backend-first (liaison services <-> UI).
2. Integrer un systeme de validation centralise (DTO + validations).
3. Ajouter des tests unitaires sur les services (`InscriptionService`, `PlanificationService`).
4. Ajouter scripts de seed SQLite pour donnees de test.
