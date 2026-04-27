package dao;

import models.Cours;
import models.Enseignant;
import models.Groupe;
import models.Salle;
import models.Seance;
import utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeanceDao {

    private final GroupeDao groupeDao = new GroupeDao();
    private final EnseignantDao enseignantDao = new EnseignantDao();
    private final SalleDao salleDao = new SalleDao();
    private final CoursDao coursDao = new CoursDao();

    public Seance save(Seance seance) {
        String sql = """
            INSERT INTO seances (id_seance, date_seance, heure_debut, heure_fin, groupe_id, enseignant_id, salle_id, cours_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, seance.getIdSeance());
            statement.setString(2, seance.getDateIso());
            statement.setString(3, seance.getHeureDebutIso());
            statement.setString(4, seance.getHeureFinIso());
            statement.setInt(5, seance.getGroupe().getId());
            statement.setInt(6, seance.getEnseignant().getId());
            statement.setInt(7, seance.getSalle().getId());
            statement.setInt(8, seance.getCours().getId());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    seance.setId(keys.getInt(1));
                }
            }

            return seance;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save Seance", e);
        }
    }

    public Optional<Seance> findById(int id) {
        String sql = "SELECT * FROM seances WHERE id = ?";

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
            throw new RuntimeException("Erreur findById Seance", e);
        }
    }

    public List<Seance> findByDate(String dateIso) {
        String sql = "SELECT * FROM seances WHERE date_seance = ? ORDER BY heure_debut";
        List<Seance> result = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, dateIso);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByDate Seance", e);
        }
    }

    public List<Seance> findAll() {
        String sql = "SELECT * FROM seances ORDER BY date_seance, heure_debut";
        List<Seance> result = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll Seance", e);
        }
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM seances WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete Seance", e);
        }
    }

    private Seance mapRow(ResultSet rs) throws SQLException {
        Groupe groupe = groupeDao.findById(rs.getInt("groupe_id"))
                .orElseThrow(() -> new IllegalStateException("Groupe introuvable pour seance"));
        Enseignant enseignant = enseignantDao.findById(rs.getInt("enseignant_id"))
                .orElseThrow(() -> new IllegalStateException("Enseignant introuvable pour seance"));
        Salle salle = salleDao.findById(rs.getInt("salle_id"))
                .orElseThrow(() -> new IllegalStateException("Salle introuvable pour seance"));
        Cours cours = coursDao.findById(rs.getInt("cours_id"))
                .orElseThrow(() -> new IllegalStateException("Cours introuvable pour seance"));

        Seance seance = new Seance(
                rs.getString("id_seance"),
                rs.getString("date_seance"),
                rs.getString("heure_debut"),
                rs.getString("heure_fin"),
                groupe,
                enseignant,
                salle,
                cours
        );
        seance.setId(rs.getInt("id"));
        return seance;
    }
}
