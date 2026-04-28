package dao;

import models.Note;
import utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NotesDao {

    public Note save(Note note) {
        String sql = """
            INSERT INTO notes (etudiant_id, cours_id, score, coefficient, date_creation)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, note.getEtudiantId());
            statement.setInt(2, note.getCoursId());
            statement.setDouble(3, note.getScore());
            statement.setDouble(4, note.getCoefficient());
            statement.setString(5, note.getDateCreation());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    note.setId(keys.getInt(1));
                }
            }

            return note;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save Note", e);
        }
    }

    public Note update(Note note) {
        String sql = """
            UPDATE notes SET score = ?, coefficient = ? WHERE id = ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, note.getScore());
            statement.setDouble(2, note.getCoefficient());
            statement.setInt(3, note.getId());
            statement.executeUpdate();

            return note;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update Note", e);
        }
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM notes WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur deleteById Note", e);
        }
    }

    public Optional<Note> findById(int id) {
        String sql = "SELECT * FROM notes WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToNote(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById Note", e);
        }

        return Optional.empty();
    }

    public Optional<Note> findByEtudiantAndCours(int etudiantId, int coursId) {
        String sql = "SELECT * FROM notes WHERE etudiant_id = ? AND cours_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, etudiantId);
            statement.setInt(2, coursId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToNote(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByEtudiantAndCours Note", e);
        }

        return Optional.empty();
    }

    public List<Note> findAll() {
        String sql = "SELECT * FROM notes";
        List<Note> notes = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                notes.add(mapResultSetToNote(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll Notes", e);
        }

        return notes;
    }

    public List<Note> findByEtudiant(int etudiantId) {
        String sql = "SELECT * FROM notes WHERE etudiant_id = ?";
        List<Note> notes = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, etudiantId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notes.add(mapResultSetToNote(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByEtudiant Notes", e);
        }

        return notes;
    }

    public List<Note> findByCours(int coursId) {
        String sql = "SELECT * FROM notes WHERE cours_id = ?";
        List<Note> notes = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, coursId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notes.add(mapResultSetToNote(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByCours Notes", e);
        }

        return notes;
    }

    private Note mapResultSetToNote(ResultSet resultSet) throws SQLException {
        return new Note(
            resultSet.getInt("id"),
            resultSet.getInt("etudiant_id"),
            resultSet.getInt("cours_id"),
            resultSet.getDouble("score"),
            resultSet.getDouble("coefficient"),
            resultSet.getString("date_creation")
        );
    }
}
