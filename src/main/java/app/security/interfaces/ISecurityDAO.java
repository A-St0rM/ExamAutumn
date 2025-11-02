package app.security.interfaces;

import app.entities.Role;
import app.entities.User;
import io.javalin.validation.ValidationException;

public interface ISecurityDAO {
    User getVerifiedUser(String username, String password) throws ValidationException; // used for login
    User createUser(String username, String password); // used for register
    Role createRole(String role);
    User addUserRole(String username, String role);
}
