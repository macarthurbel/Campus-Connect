package dao;

import models.Enseignant;
import models.Groupe;
import models.TypeGroupe;
import utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GroupeDao {

    private final EnseignantDao enseignantDao = new EnseignantDao();

    public Groupe save(Groupe groupe) {
        String sql = """
            INSERT INTO groupes (nom, capacite, enseignant_id, type_groupe)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, groupe.getNom());
            statement.setInt(2, groupe.getCapacite());
            if (groupe.getEnseignant() != null && groupe.getEnseignant().getId() > 0) {
                statement.setInt(3, groupe.getEnseignant().getId());
            } else {
                statement.setNull(3, java.sql.Types.INTEGER);
            }
            statement.setString(4, groupe.getType().name());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    groupe.setId(keys.getInt(1));
                }
            }

            return groupe;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save Groupe", e);
        }
    }

    public Optional<Groupe> findById(int id) {
        String sql = "SELECT * FROM groupes WHERE id = ?";

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
            throw new RuntimeException("Erreur findById Groupe", e);
        }
    }

    public List<Groupe> findAll() {
        String sql = "SELECT * FROM groupes ORDER BY nom";
        List<Groupe> result = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll Groupe", e);
        }
    }

    public boolean update(Groupe groupe) {
        String sql = """
            UPDATE groupes
            SET nom = ?, capacite = ?, enseignant_id = ?, type_groupe = ?
            WHERE id = ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, groupe.getNom());
            statement.setInt(2, groupe.getCapacite());
            if (groupe.getEnseignant() != null && groupe.getEnseignant().getId() > 0) {
                statement.setInt(3, groupe.getEnseignant().getId());
            } else {
                statement.setNull(3, java.sql.Types.INTEGER);
            }
            statement.setString(4, groupe.getType().name());
            statement.setInt(5, groupe.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update Groupe", e);
        }
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM groupes WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete Groupe", e);
        }
    }

    private Groupe mapRow(ResultSet rs) throws SQLException {
        int enseignantId = rs.getInt("enseignant_id");
        Enseignant enseignant = null;
        if (!rs.wasNull()) {
            enseignant = enseignantDao.findById(enseignantId).orElse(null);
        }

        Groupe groupe = new Groupe(
                rs.getString("nom"),
                rs.getInt("capacite"),
                enseignant,
                TypeGroupe.valueOf(rs.getString("type_groupe"))
        );
        groupe.setId(rs.getInt("id"));
        return groupe;
    }
}
