# CampusConnect Connected App

This folder contains the connected JavaFX frontend + Java backend app.

## What Is Connected

- JavaFX UI (login + role dashboards)
- Backend domain models
- DAO layer
- Services layer
- SQLite database bootstrap at app startup

## Prerequisites

- Java JDK 17+
- Maven 3.9+

## Run

From this folder:

```bash
mvn javafx:run
```

## Build Check

```bash
mvn -DskipTests compile
```

## Database Configuration

- Engine: SQLite
- JDBC URL: `jdbc:sqlite:data/projet_java.db`
- Username: not required
- Password: not required
- DB file location: `data/projet_java.db` (created automatically)

## First Start Behavior

At startup, the app initializes the backend and creates tables if missing:

- enseignants
- etudiants
- groupes
- cours
- salles
- seances
- inscriptions

## Notes

- This is local development mode (file-based SQLite).
- No external DB server or credentials are needed.
