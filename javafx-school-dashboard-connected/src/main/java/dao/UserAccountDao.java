package dao;

import models.UserAccount;
import models.UserRole;
import utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserAccountDao {

    public UserAccount save(UserAccount account) {
        String sql = """
            INSERT INTO users (username, password_hash, role, linked_entity_id)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, account.getUsername());
            statement.setString(2, account.getPasswordHash());
            statement.setString(3, account.getRole().name());
            if (account.getLinkedEntityId() == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, account.getLinkedEntityId());
            }
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    account.setId(keys.getInt(1));
                }
            }

            return account;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save UserAccount", e);
        }
    }

    public Optional<UserAccount> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByUsername UserAccount", e);
        }
    }

    public Optional<UserAccount> findAdmin() {
        return findByUsername("admin");
    }

    public Optional<UserAccount> findByRoleAndLinkedEntityId(UserRole role, int linkedEntityId) {
        String sql = "SELECT * FROM users WHERE role = ? AND linked_entity_id = ? LIMIT 1";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, role.name());
            statement.setInt(2, linkedEntityId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByRoleAndLinkedEntityId UserAccount", e);
        }
    }

    public List<UserAccount> findAll() {
        String sql = "SELECT * FROM users ORDER BY role, username";
        List<UserAccount> result = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll UserAccount", e);
        }
    }

    public boolean updatePassword(String username, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, passwordHash);
            statement.setString(2, username);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur updatePassword UserAccount", e);
        }
    }

    public boolean deleteByUsername(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete UserAccount", e);
        }
    }

    public boolean deleteByRoleAndLinkedEntityId(UserRole role, int linkedEntityId) {
        String sql = "DELETE FROM users WHERE role = ? AND linked_entity_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, role.name());
            statement.setInt(2, linkedEntityId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur deleteByRoleAndLinkedEntityId UserAccount", e);
        }
    }

    public boolean updateUsernameById(int id, String username) {
        String sql = "UPDATE users SET username = ? WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setInt(2, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur updateUsernameById UserAccount", e);
        }
    }

    public boolean updateUsernameAndPasswordById(int id, String username, String passwordHash) {
        String sql = "UPDATE users SET username = ?, password_hash = ? WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setInt(3, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur updateUsernameAndPasswordById UserAccount", e);
        }
    }

    private UserAccount mapRow(ResultSet rs) throws SQLException {
        UserAccount account = new UserAccount(
            rs.getString("username"),
            rs.getString("password_hash"),
            UserRole.valueOf(rs.getString("role")),
            rs.getObject("linked_entity_id") == null ? null : rs.getInt("linked_entity_id")
        );
        account.setId(rs.getInt("id"));
        return account;
    }
}
