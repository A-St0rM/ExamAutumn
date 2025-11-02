package app.security.interfaces;

import app.entities.Role;

public interface ISecurityUser {
    boolean verifyPassword(String pw);
    void addRole(Role role);
    void removeRole(String role);
}
