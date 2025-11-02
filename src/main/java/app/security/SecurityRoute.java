package app.security;


import app.routes.Route;
import app.security.interfaces.ISecurityController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class SecurityRoute {

    ISecurityController securityController = new SecurityController();

    public EndpointGroup getSecurityRoutes () {
        return () -> {
            get("/healthcheck", securityController::healthCheck);
            post("/login", securityController.login());
            post("/register", securityController.register());
        };
    }
}
