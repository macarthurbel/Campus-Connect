package dao;

import models.Enseignant;
import models.StatutEnseignant;
import utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnseignantDao {

    public Enseignant save(Enseignant enseignant) {
        String sql = """
            INSERT INTO enseignants (nom, prenom, email, date_naissance, departement, statut)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, enseignant.getNom());
            statement.setString(2, enseignant.getPrenom());
            statement.setString(3, enseignant.getEmail());
            statement.setString(4, enseignant.getDateNaissanceIso());
            statement.setString(5, enseignant.getDepartement());
            statement.setString(6, enseignant.getStatut().name());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    enseignant.setId(keys.getInt(1));
                }
            }

            return enseignant;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save Enseignant", e);
        }
    }

    public Optional<Enseignant> findById(int id) {
        String sql = "SELECT * FROM enseignants WHERE id = ?";

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
            throw new RuntimeException("Erreur findById Enseignant", e);
        }
    }

    public List<Enseignant> findAll() {
        String sql = "SELECT * FROM enseignants ORDER BY nom, prenom";
        List<Enseignant> result = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll Enseignant", e);
        }
    }

    public boolean update(Enseignant enseignant) {
        String sql = """
            UPDATE enseignants
            SET nom = ?, prenom = ?, email = ?, date_naissance = ?, departement = ?, statut = ?
            WHERE id = ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, enseignant.getNom());
            statement.setString(2, enseignant.getPrenom());
            statement.setString(3, enseignant.getEmail());
            statement.setString(4, enseignant.getDateNaissanceIso());
            statement.setString(5, enseignant.getDepartement());
            statement.setString(6, enseignant.getStatut().name());
            statement.setInt(7, enseignant.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update Enseignant", e);
        }
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM enseignants WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete Enseignant", e);
        }
    }

    private Enseignant mapRow(ResultSet rs) throws SQLException {
        Enseignant enseignant = new Enseignant(
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("email"),
                rs.getString("date_naissance"),
                rs.getString("departement"),
                StatutEnseignant.valueOf(rs.getString("statut"))
        );
        enseignant.setId(rs.getInt("id"));
        return enseignant;
    }
}
