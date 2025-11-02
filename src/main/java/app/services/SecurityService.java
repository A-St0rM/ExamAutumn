package app.services;

import app.entities.User;
import app.exceptions.ValidationException;
import app.security.interfaces.ISecurityDAO;
import dk.bugelhartmann.UserDTO;

import javax.management.relation.Role;
import java.util.Set;
import java.util.stream.Collectors;

public class SecurityService {
    private final ISecurityDAO securityDAO;

    public SecurityService(ISecurityDAO securityDAO) {
        this.securityDAO = securityDAO;
    }

    public UserDTO login(String username, String password) throws ValidationException {
        User verified = securityDAO.getVerifiedUser(username, password);

        Set<String> roles = verified.getRoles()
                .stream()
                .map(role -> role.getRolename())
                .collect(Collectors.toSet());

        return new UserDTO(verified.getUsername(), roles);
    }


    public UserDTO register(String username, String password) {
        User created = securityDAO.createUser(username, password);
        try { securityDAO.createRole("USER"); } catch (Exception ignored) {}
        User updated = securityDAO.addUserRole(created.getUsername(), "USER");

        Set<String> roles = updated.getRoles().stream()
                .map(r -> r.getRolename())
                .collect(Collectors.toSet());

        // defensiv guard: sikre at roles ikke er tom
        if (roles.isEmpty()) {
            throw new app.exceptions.ValidationException("Registered user has no roles assigned");
        }
        return new UserDTO(updated.getUsername(), roles);
    }

    public void assignRole(String username, String role) {
        securityDAO.addUserRole(username, role);
    }

    public boolean userHasAllowedRole(UserDTO user, Set<String> allowedRoles) {
        return user.getRoles().stream()
                .anyMatch(role -> allowedRoles.contains(role.toUpperCase()));
    }

    public boolean isOpenEndpoint(Set<String> allowedRoles) {
        // If the endpoint is not protected with any roles:
        if (allowedRoles.isEmpty())
            return true;

        // 1. Get permitted roles and Check if the endpoint is open to all with the ANYONE role
        if (allowedRoles.contains("ANYONE")) {
            return true;
        }
        return false;
    }
}
