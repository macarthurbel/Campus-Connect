package services;

import dao.UserAccountDao;
import models.UserAccount;
import models.UserRole;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

public class AuthService {

    private final UserAccountDao userAccountDao;

    public AuthService() {
        this(new UserAccountDao());
    }

    public AuthService(UserAccountDao userAccountDao) {
        this.userAccountDao = userAccountDao;
    }

    public void seedAdminIfMissing() {
        if (userAccountDao.findAdmin().isEmpty()) {
            userAccountDao.save(new UserAccount("admin", hash("admin123"), UserRole.ADMIN, null));
        }
    }

    public UserAccount authenticate(String roleLabel, String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Identifiant et mot de passe requis");
        }

        Optional<UserAccount> account = userAccountDao.findByUsername(username.trim());
        if (account.isEmpty()) {
            throw new IllegalStateException("Compte introuvable");
        }

        UserAccount userAccount = account.get();
        if (roleLabel != null && !roleMatches(roleLabel, userAccount.getRole())) {
            throw new IllegalStateException("Role non autorise pour ce compte");
        }

        if (!userAccount.getPasswordHash().equals(hash(password))) {
            throw new IllegalStateException("Mot de passe invalide");
        }

        return userAccount;
    }

    public boolean deleteAccount(String username) {
        return userAccountDao.deleteByUsername(username);
    }

    public boolean changePassword(String username, String newPassword) {
        return userAccountDao.updatePassword(username, hash(newPassword));
    }

    public boolean resetPassword(String roleLabel, String username, String newPassword) {
        if (username == null || username.isBlank() || newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("Identifiant et nouveau mot de passe requis");
        }

        Optional<UserAccount> account = userAccountDao.findByUsername(username.trim());
        if (account.isEmpty()) {
            throw new IllegalStateException("Compte introuvable");
        }

        UserAccount userAccount = account.get();
        if (roleLabel != null && !roleMatches(roleLabel, userAccount.getRole())) {
            throw new IllegalStateException("Role non autorise pour ce compte");
        }

        return userAccountDao.updatePassword(userAccount.getUsername(), hash(newPassword));
    }

    public Optional<String> findLinkedIdentifier(UserRole role, int linkedEntityId) {
        return userAccountDao.findByRoleAndLinkedEntityId(role, linkedEntityId).map(UserAccount::getUsername);
    }

    public void upsertLinkedCredentials(UserRole role, int linkedEntityId, String username, String plainPassword) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Identifiant requis");
        }

        String normalizedUsername = username.trim();
        String normalizedPassword = plainPassword == null ? "" : plainPassword.trim();

        Optional<UserAccount> linkedAccount = userAccountDao.findByRoleAndLinkedEntityId(role, linkedEntityId);
        Optional<UserAccount> byUsername = userAccountDao.findByUsername(normalizedUsername);

        if (byUsername.isPresent() && (linkedAccount.isEmpty() || byUsername.get().getId() != linkedAccount.get().getId())) {
            throw new IllegalStateException("Identifiant deja utilise");
        }

        if (linkedAccount.isPresent()) {
            UserAccount existing = linkedAccount.get();
            boolean updated;
            if (normalizedPassword.isBlank()) {
                updated = userAccountDao.updateUsernameById(existing.getId(), normalizedUsername);
            } else {
                updated = userAccountDao.updateUsernameAndPasswordById(existing.getId(), normalizedUsername, hash(normalizedPassword));
            }

            if (!updated) {
                throw new IllegalStateException("Impossible de mettre a jour les identifiants");
            }
            return;
        }

        if (normalizedPassword.isBlank()) {
            throw new IllegalArgumentException("Mot de passe requis pour creer le compte");
        }

        userAccountDao.save(new UserAccount(normalizedUsername, hash(normalizedPassword), role, linkedEntityId));
    }

    public boolean deleteLinkedCredentials(UserRole role, int linkedEntityId) {
        return userAccountDao.deleteByRoleAndLinkedEntityId(role, linkedEntityId);
    }

    private boolean roleMatches(String roleLabel, UserRole role) {
        return switch (roleLabel) {
            case "Administrateur" -> role == UserRole.ADMIN;
            case "Enseignant" -> role == UserRole.TEACHER;
            case "Etudiant" -> role == UserRole.STUDENT;
            default -> false;
        };
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
