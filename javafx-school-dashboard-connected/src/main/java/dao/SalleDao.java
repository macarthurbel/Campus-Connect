package dao;

import models.Salle;
import models.TypeSalle;
import utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SalleDao {

    public Salle save(Salle salle) {
        String sql = """
            INSERT INTO salles (identifiant_salle, capacite_accueil, type_salle)
            VALUES (?, ?, ?)
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, salle.getIdentifiantSalle());
            statement.setInt(2, salle.getCapaciteAccueil());
            statement.setString(3, salle.getTypeSalle().name());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    salle.setId(keys.getInt(1));
                }
            }

            return salle;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save Salle", e);
        }
    }

    public Optional<Salle> findById(int id) {
        String sql = "SELECT * FROM salles WHERE id = ?";

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
            throw new RuntimeException("Erreur findById Salle", e);
        }
    }

    public List<Salle> findAll() {
        String sql = "SELECT * FROM salles ORDER BY identifiant_salle";
        List<Salle> result = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll Salle", e);
        }
    }

    public boolean update(Salle salle) {
        String sql = """
            UPDATE salles
            SET identifiant_salle = ?, capacite_accueil = ?, type_salle = ?
            WHERE id = ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, salle.getIdentifiantSalle());
            statement.setInt(2, salle.getCapaciteAccueil());
            statement.setString(3, salle.getTypeSalle().name());
            statement.setInt(4, salle.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update Salle", e);
        }
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM salles WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete Salle", e);
        }
    }

    private Salle mapRow(ResultSet rs) throws SQLException {
        Salle salle = new Salle(
                rs.getString("identifiant_salle"),
                rs.getInt("capacite_accueil"),
                TypeSalle.valueOf(rs.getString("type_salle"))
        );
        salle.setId(rs.getInt("id"));
        return salle;
    }
}
