package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {

    private static final String DB_DIRECTORY = "data";
    private static final String DB_URL = "jdbc:sqlite:" + DB_DIRECTORY + "/projet_java.db";

    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DB_URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public static void initializeDatabase() {
        createDatabaseDirectory();

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("""
                CREATE TABLE IF NOT EXISTS enseignants (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nom TEXT NOT NULL,
                    prenom TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    date_naissance TEXT NOT NULL,
                    departement TEXT NOT NULL,
                    statut TEXT NOT NULL
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS etudiants (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nom TEXT NOT NULL,
                    prenom TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    date_naissance TEXT NOT NULL,
                    matricule TEXT NOT NULL UNIQUE,
                    filiere TEXT NOT NULL,
                    annee INTEGER NOT NULL
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS groupes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nom TEXT NOT NULL UNIQUE,
                    capacite INTEGER NOT NULL,
                    enseignant_id INTEGER,
                    type_groupe TEXT NOT NULL,
                    FOREIGN KEY (enseignant_id) REFERENCES enseignants(id) ON DELETE SET NULL
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS cours (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    code TEXT NOT NULL UNIQUE,
                    intitule TEXT NOT NULL,
                    description TEXT NOT NULL,
                    volume_horaire INTEGER NOT NULL,
                    enseignant_responsable_id INTEGER,
                    FOREIGN KEY (enseignant_responsable_id) REFERENCES enseignants(id) ON DELETE SET NULL
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS salles (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    identifiant_salle TEXT NOT NULL UNIQUE,
                    capacite_accueil INTEGER NOT NULL,
                    type_salle TEXT NOT NULL
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS seances (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_seance TEXT NOT NULL UNIQUE,
                    date_seance TEXT NOT NULL,
                    heure_debut TEXT NOT NULL,
                    heure_fin TEXT NOT NULL,
                    groupe_id INTEGER NOT NULL,
                    enseignant_id INTEGER NOT NULL,
                    salle_id INTEGER NOT NULL,
                    cours_id INTEGER NOT NULL,
                    FOREIGN KEY (groupe_id) REFERENCES groupes(id) ON DELETE CASCADE,
                    FOREIGN KEY (enseignant_id) REFERENCES enseignants(id) ON DELETE CASCADE,
                    FOREIGN KEY (salle_id) REFERENCES salles(id) ON DELETE CASCADE,
                    FOREIGN KEY (cours_id) REFERENCES cours(id) ON DELETE CASCADE
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS inscriptions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    etudiant_id INTEGER NOT NULL,
                    groupe_id INTEGER NOT NULL,
                    date_inscription TEXT NOT NULL,
                    moyenne REAL NOT NULL DEFAULT 0,
                    note REAL NOT NULL DEFAULT 0,
                    UNIQUE (etudiant_id, groupe_id),
                    FOREIGN KEY (etudiant_id) REFERENCES etudiants(id) ON DELETE CASCADE,
                    FOREIGN KEY (groupe_id) REFERENCES groupes(id) ON DELETE CASCADE
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL,
                    linked_entity_id INTEGER,
                    enabled INTEGER NOT NULL DEFAULT 1
                )
                """);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'initialisation de la base SQLite", e);
        }
    }

    private static void createDatabaseDirectory() {
        try {
            Files.createDirectories(Path.of(DB_DIRECTORY));
        } catch (IOException e) {
            throw new RuntimeException("Impossible de creer le dossier de la base", e);
        }
    }
}
