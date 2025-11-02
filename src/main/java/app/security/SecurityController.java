package app.security;

import app.config.HibernateConfig;
import app.entities.User;
import app.exceptions.ValidationException;
import app.security.interfaces.ISecurityController;
import app.security.interfaces.ISecurityDAO;
import app.services.SecurityService;
import app.services.TokenService;
import app.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.*;
import dk.bugelhartmann.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import java.util.stream.Collectors;


public class SecurityController implements ISecurityController {
    ISecurityDAO securityDAO = new SecurityDAO(HibernateConfig.getEntityManagerFactory());
    ObjectMapper objectMapper = new Utils().getObjectMapper();
    private final SecurityService securityService;
    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);
    private TokenService tokenService = new TokenService();

    public SecurityController() {
        this.securityService = new SecurityService(
                new SecurityDAO(HibernateConfig.getEntityManagerFactory())
        );
    }


    public void healthCheck(Context ctx) {
        ctx.status(200).json("{\"msg\": \"API is up and running\"}");
    }


    public Handler login() {
        return ctx -> {
            User user = ctx.bodyAsClass(User.class);
            try {
                UserDTO dto = securityService.login(user.getUsername(), user.getPassword());
                String token = tokenService.createToken(dto);

                ctx.json(objectMapper.createObjectNode()
                                .put("token", token)
                                .put("username", dto.getUsername()))
                        .status(200);
            } catch (ValidationException e) {
                ctx.json(objectMapper.createObjectNode()
                                .put("msg", "Wrong username or password"))
                        .status(401);
            }
        };
    }


    public Handler register() {
        return ctx -> {
            User user = ctx.bodyAsClass(User.class);
            UserDTO dto = securityService.register(user.getUsername(), user.getPassword());
            String token = tokenService.createToken(dto);

            ctx.json(objectMapper.createObjectNode()
                            .put("token", token)
                            .put("username", dto.getUsername()))
                    .status(201);
        };
    }

    @Override
    public Handler authenticate() {
        return (Context ctx) -> {// This is a preflight request => no need for authentication
            if (ctx.method().toString().equals("OPTIONS")) {
                ctx.status(200);
                return;
            }
            // If the endpoint is not protected with roles or is open to ANYONE role, then skip
            Set<String> allowedRoles = ctx.routeRoles().stream().map(role -> role.toString().toUpperCase()).collect(Collectors.toSet());
            if (securityService.isOpenEndpoint(allowedRoles))
                return;

            // If there is no token we do not allow entry
            UserDTO verifiedTokenUser = validateAndGetUserFromToken(ctx);
            ctx.attribute("user", verifiedTokenUser); // -> ctx.attribute("user") in ApplicationConfig beforeMatched filter
        };
    }

    public boolean authorize(UserDTO userDTO, Set<String> allowedRoles) {
        return false;
    }

    @Override
    public Handler authorize() {
        return (Context ctx) -> {

            Set<String> allowedRoles = ctx.routeRoles()
                    .stream()
                    .map(role -> role.toString().toUpperCase())
                    .collect(Collectors.toSet());

            // 1. Check if the endpoint is open to all (either by not having any roles or having the ANYONE role set
            if (securityService.isOpenEndpoint(allowedRoles))
                return;
            // 2. Get user and ensure it is not null
            UserDTO user = ctx.attribute("user");
            if (user == null) {
                throw new ForbiddenResponse("No user was added from the token");
            }
            // 3. See if any role matches
            if (!securityService.userHasAllowedRole(user, allowedRoles))
                throw new ForbiddenResponse("User was not authorized with roles: " + user.getRoles() + ". Needed roles are: " + allowedRoles);
        };
    }



    private static String getToken(Context ctx) {
        String header = ctx.header("Authorization");
        if (header == null) {
            throw new UnauthorizedResponse("Authorization header is missing"); // UnauthorizedResponse is javalin 6 specific but response is not json!
        }

        // If the Authorization Header was malformed, then no entry
        String token = header.split(" ")[1];
        if (token == null) {
            throw new UnauthorizedResponse("Authorization header is malformed"); // UnauthorizedResponse is javalin 6 specific but response is not json!
        }
        return token;
    }


    private UserDTO validateAndGetUserFromToken(Context ctx) {
        String token = getToken(ctx);
        UserDTO verifiedTokenUser = tokenService.verifyToken(token);
        if (verifiedTokenUser == null) {
            throw new UnauthorizedResponse("Invalid user or token"); // UnauthorizedResponse is javalin 6 specific but response is not json!
        }
        return verifiedTokenUser;
    }


}