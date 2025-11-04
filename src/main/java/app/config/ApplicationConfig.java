package app.config;

import app.Main;
import app.controllers.TripController;
import app.routes.Route;
import app.routes.TripRoute;
import app.security.SecurityController;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.HttpStatus;
import io.javalin.http.InternalServerErrorResponse;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ApplicationConfig {
    private static Route routes = new Route();

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);
    private static final Logger debugLogger = LoggerFactory.getLogger("app");
    private static SecurityController securityController = new SecurityController();
    private static ApplicationConfig appConfig;

    public static void configuration(JavalinConfig config){
        config.showJavalinBanner = false;
        config.bundledPlugins.enableRouteOverview("/routes");
        config.router.contextPath = "/api/v1"; // base path for all endpoints
        config.router.apiBuilder(routes.getRoutes());
    }

    public static ApplicationConfig startServer(int port) {
        routes = new Route();

        //DI (Best practice)
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        var objectMapper = new app.utils.Utils().getObjectMapper();
        var apiService   = new app.services.ApiService();
        var packingClient = new app.Integrations.PackingApiClient( apiService,
                objectMapper,
                "https://packingapi.cphbusinessapps.dk"
        );

        TripController tripController = new TripController(emf, packingClient);
        TripRoute tripRoute = new TripRoute(tripController);

        routes.setTripRoute(tripRoute);

        var app = Javalin.create(ApplicationConfig::configuration);

        logger.info("Java application started!");

        app.get("/", ctx -> {
            logger.info("Handling request to /");
            ctx.result("Hello, Javalin with Logging!");
        });

        app.get("/error", ctx -> {
            logger.error("An error endpoint was accessed");
            throw new RuntimeException("This is an intentional error for logging demonstration.");
        });

        app.before(ctx -> {
            logger.info("Received {} request to {}", ctx.method(), ctx.path());
        });

        // Global exception mapping
        app.exception(app.exceptions.EntityNotFoundException.class, (e, ctx) -> {
            logger.error("Entity not found: {}", e.getMessage());
            ctx.status(404).json(Map.of("error","ENTITY_NOT_FOUND","message",e.getMessage()));
        });
        app.exception(app.exceptions.ValidationException.class, (e, ctx) -> {
            logger.error("Validation error: {}", e.getMessage());
            ctx.status(400).json(Map.of("error","VALIDATION_ERROR","message",e.getMessage()));
        });
        app.exception(app.exceptions.DatabaseException.class, (e, ctx) -> {
            logger.error("Database error: {}", e.getMessage());
            ctx.status(500).json(Map.of("error","DATABASE_ERROR","message",e.getMessage()));
        });

        app.exception(io.javalin.http.UnauthorizedResponse.class, (e, ctx) -> {
            ctx.status(401).json(Map.of("error","UNAUTHORIZED","message", e.getMessage()));
        });
        app.exception(io.javalin.http.ForbiddenResponse.class, (e, ctx) -> {
            ctx.status(403).json(Map.of("error","FORBIDDEN","message", e.getMessage()));
        });

        // Catch-all exception handler
        app.exception(Exception.class, (e, ctx) -> {
            logger.error("Unhandled exception at {} {}", ctx.method(), ctx.path(), e);
            ctx.status(500).json(Map.of("error", "INTERNAL_SERVER_ERROR", "message", "An unexpected error occurred"));
        });

        // Error handler for 500 errors
        app.error(HttpStatus.INTERNAL_SERVER_ERROR, ctx -> {
            logger.error("Internal server error at {} {}", ctx.method(), ctx.path());
            ctx.json(Map.of("error", "INTERNAL_SERVER_ERROR", "message", "Off limits!"));
        });


        app.beforeMatched(securityController.authenticate());
        app.beforeMatched(securityController.authorize());

        app.start(port);
        return appConfig;
    }


    //Overloaded method for test
    public static ApplicationConfig startServer(int port, app.Integrations.PackingApiClient packingClient) {
        routes = new app.routes.Route();


        var emf = HibernateConfig.getEntityManagerFactory();
        var tripController = new app.controllers.TripController(emf, packingClient);
        var tripRoute = new app.routes.TripRoute(tripController);
        routes.setTripRoute(tripRoute);

        var app = Javalin.create(ApplicationConfig::configuration);

        logger.info("Java application started!");

        app.get("/", ctx -> {
            logger.info("Handling request to /");
            ctx.result("Hello, Javalin with Logging!");
        });

        app.get("/error", ctx -> {
            logger.error("An error endpoint was accessed");
            throw new RuntimeException("This is an intentional error for logging demonstration.");
        });

        app.before(ctx -> {
            logger.info("Received {} request to {}", ctx.method(), ctx.path());
        });

        // Global exception mapping
        app.exception(app.exceptions.EntityNotFoundException.class, (e, ctx) -> {
            logger.error("Entity not found: {}", e.getMessage());
            ctx.status(404).json(Map.of("error","ENTITY_NOT_FOUND","message",e.getMessage()));
        });
        app.exception(app.exceptions.ValidationException.class, (e, ctx) -> {
            logger.error("Validation error: {}", e.getMessage());
            ctx.status(400).json(Map.of("error","VALIDATION_ERROR","message",e.getMessage()));
        });
        app.exception(app.exceptions.DatabaseException.class, (e, ctx) -> {
            logger.error("Database error: {}", e.getMessage());
            ctx.status(500).json(Map.of("error","DATABASE_ERROR","message",e.getMessage()));
        });

        // Catch-all exception handler
        app.exception(Exception.class, (e, ctx) -> {
            logger.error("Unhandled exception at {} {}", ctx.method(), ctx.path(), e);
            ctx.status(500).json(Map.of("error", "INTERNAL_SERVER_ERROR", "message", "An unexpected error occurred"));
        });

        // Error handler for 500 errors
        app.error(HttpStatus.INTERNAL_SERVER_ERROR, ctx -> {
            logger.error("Internal server error at {} {}", ctx.method(), ctx.path());
            ctx.json(Map.of("error", "INTERNAL_SERVER_ERROR", "message", "Off limits!"));
        });

        app.beforeMatched(securityController.authenticate());
        app.beforeMatched(securityController.authorize());

        app.start(port);
        return appConfig;
    }



    public static void stopServer(Javalin app) {
        app.stop();
    }
}
