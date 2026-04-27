package dao;

import models.Etudiant;
import models.Groupe;
import models.Inscription;
import utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InscriptionDao {

    private final EtudiantDao etudiantDao = new EtudiantDao();
    private final GroupeDao groupeDao = new GroupeDao();

    public Inscription save(Inscription inscription) {
        String sql = """
            INSERT INTO inscriptions (etudiant_id, groupe_id, date_inscription, moyenne, note)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, inscription.getEtudiant().getId());
            statement.setInt(2, inscription.getGroupe().getId());
            statement.setString(3, inscription.getDateInscriptionIso());
            statement.setFloat(4, inscription.getMoyenne());
            statement.setFloat(5, inscription.getNote());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    inscription.setId(keys.getInt(1));
                }
            }

            return inscription;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save Inscription", e);
        }
    }

    public Optional<Inscription> findById(int id) {
        String sql = "SELECT * FROM inscriptions WHERE id = ?";

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
            throw new RuntimeException("Erreur findById Inscription", e);
        }
    }

    public Optional<Inscription> findByEtudiantAndGroupe(int etudiantId, int groupeId) {
        String sql = "SELECT * FROM inscriptions WHERE etudiant_id = ? AND groupe_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, etudiantId);
            statement.setInt(2, groupeId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByEtudiantAndGroupe Inscription", e);
        }
    }

    public int countByGroupe(int groupeId) {
        String sql = "SELECT COUNT(*) AS total FROM inscriptions WHERE groupe_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, groupeId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur countByGroupe Inscription", e);
        }
    }

    public List<Inscription> findAll() {
        String sql = "SELECT * FROM inscriptions ORDER BY date_inscription DESC";
        List<Inscription> result = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll Inscription", e);
        }
    }

    public boolean update(Inscription inscription) {
        String sql = """
            UPDATE inscriptions
            SET moyenne = ?, note = ?
            WHERE id = ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setFloat(1, inscription.getMoyenne());
            statement.setFloat(2, inscription.getNote());
            statement.setInt(3, inscription.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update Inscription", e);
        }
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM inscriptions WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete Inscription", e);
        }
    }

    private Inscription mapRow(ResultSet rs) throws SQLException {
        Etudiant etudiant = etudiantDao.findById(rs.getInt("etudiant_id"))
                .orElseThrow(() -> new IllegalStateException("Etudiant introuvable pour inscription"));
        Groupe groupe = groupeDao.findById(rs.getInt("groupe_id"))
                .orElseThrow(() -> new IllegalStateException("Groupe introuvable pour inscription"));

        Inscription inscription = new Inscription(
                etudiant,
                groupe,
                rs.getString("date_inscription")
        );
        inscription.setId(rs.getInt("id"));
        inscription.setMoyenne(rs.getFloat("moyenne"));
        inscription.setNote(rs.getFloat("note"));
        return inscription;
    }
}
