package models;

import java.util.Objects;

public class UserAccount {

    private int id;
    private String username;
    private String passwordHash;
    private UserRole role;
    private Integer linkedEntityId;

    public UserAccount(String username, String passwordHash, UserRole role, Integer linkedEntityId) {
        this.username = Objects.requireNonNull(username, "username requis");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash requis");
        this.role = Objects.requireNonNull(role, "role requis");
        this.linkedEntityId = linkedEntityId;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public Integer getLinkedEntityId() { return linkedEntityId; }

    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = Objects.requireNonNull(username, "username requis"); }
    public void setPasswordHash(String passwordHash) { this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash requis"); }
    public void setRole(UserRole role) { this.role = Objects.requireNonNull(role, "role requis"); }
    public void setLinkedEntityId(Integer linkedEntityId) { this.linkedEntityId = linkedEntityId; }
}
