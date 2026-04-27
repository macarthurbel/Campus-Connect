package dao;

import models.Etudiant;
import utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EtudiantDao {

    public Etudiant save(Etudiant etudiant) {
        String sql = """
            INSERT INTO etudiants (nom, prenom, email, date_naissance, matricule, filiere, annee)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, etudiant.getNom());
            statement.setString(2, etudiant.getPrenom());
            statement.setString(3, etudiant.getEmail());
            statement.setString(4, etudiant.getDateNaissanceIso());
            statement.setString(5, etudiant.getMatricule());
            statement.setString(6, etudiant.getFiliere());
            statement.setInt(7, etudiant.getAnnee());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    etudiant.setId(keys.getInt(1));
                }
            }

            return etudiant;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save Etudiant", e);
        }
    }

    public Optional<Etudiant> findById(int id) {
        String sql = "SELECT * FROM etudiants WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById Etudiant", e);
        }
    }

    public Optional<Etudiant> findByMatricule(String matricule) {
        String sql = "SELECT * FROM etudiants WHERE matricule = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, matricule);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByMatricule Etudiant", e);
        }
    }

    public List<Etudiant> findAll() {
        String sql = "SELECT * FROM etudiants ORDER BY nom, prenom";
        List<Etudiant> result = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll Etudiant", e);
        }
    }

    public boolean update(Etudiant etudiant) {
        String sql = """
            UPDATE etudiants
            SET nom = ?, prenom = ?, email = ?, date_naissance = ?, matricule = ?, filiere = ?, annee = ?
            WHERE id = ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, etudiant.getNom());
            statement.setString(2, etudiant.getPrenom());
            statement.setString(3, etudiant.getEmail());
            statement.setString(4, etudiant.getDateNaissanceIso());
            statement.setString(5, etudiant.getMatricule());
            statement.setString(6, etudiant.getFiliere());
            statement.setInt(7, etudiant.getAnnee());
            statement.setInt(8, etudiant.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update Etudiant", e);
        }
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM etudiants WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete Etudiant", e);
        }
    }

    private Etudiant mapRow(ResultSet rs) throws SQLException {
        Etudiant etudiant = new Etudiant(
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("email"),
                rs.getString("date_naissance"),
                rs.getString("matricule"),
                rs.getString("filiere"),
                rs.getInt("annee")
        );
        etudiant.setId(rs.getInt("id"));
        return etudiant;
    }
}
