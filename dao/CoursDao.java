package dao;

import models.Cours;
import models.Enseignant;
import utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CoursDao {

    private final EnseignantDao enseignantDao = new EnseignantDao();

    public Cours save(Cours cours) {
        String sql = """
            INSERT INTO cours (code, intitule, description, volume_horaire, enseignant_responsable_id)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, cours.getCode());
            statement.setString(2, cours.getIntitule());
            statement.setString(3, cours.getDescription());
            statement.setInt(4, cours.getVolumeHoraire());
            if (cours.getEnseignantResponsable() != null && cours.getEnseignantResponsable().getId() > 0) {
                statement.setInt(5, cours.getEnseignantResponsable().getId());
            } else {
                statement.setNull(5, java.sql.Types.INTEGER);
            }
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    cours.setId(keys.getInt(1));
                }
            }

            return cours;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save Cours", e);
        }
    }

    public Optional<Cours> findById(int id) {
        String sql = "SELECT * FROM cours WHERE id = ?";

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
            throw new RuntimeException("Erreur findById Cours", e);
        }
    }

    public List<Cours> findAll() {
        String sql = "SELECT * FROM cours ORDER BY code";
        List<Cours> result = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll Cours", e);
        }
    }

    public boolean update(Cours cours) {
        String sql = """
            UPDATE cours
            SET code = ?, intitule = ?, description = ?, volume_horaire = ?, enseignant_responsable_id = ?
            WHERE id = ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cours.getCode());
            statement.setString(2, cours.getIntitule());
            statement.setString(3, cours.getDescription());
            statement.setInt(4, cours.getVolumeHoraire());
            if (cours.getEnseignantResponsable() != null && cours.getEnseignantResponsable().getId() > 0) {
                statement.setInt(5, cours.getEnseignantResponsable().getId());
            } else {
                statement.setNull(5, java.sql.Types.INTEGER);
            }
            statement.setInt(6, cours.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update Cours", e);
        }
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM cours WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete Cours", e);
        }
    }

    private Cours mapRow(ResultSet rs) throws SQLException {
        int enseignantId = rs.getInt("enseignant_responsable_id");
        Enseignant enseignant = null;
        if (!rs.wasNull()) {
            enseignant = enseignantDao.findById(enseignantId).orElse(null);
        }

        Cours cours = new Cours(
                rs.getString("code"),
                rs.getString("intitule"),
                rs.getString("description"),
                rs.getInt("volume_horaire"),
                enseignant
        );
        cours.setId(rs.getInt("id"));
        return cours;
    }
}
