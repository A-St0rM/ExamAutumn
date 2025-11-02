package app.routes;

import app.controllers.TripController;
import io.javalin.apibuilder.EndpointGroup;
import static app.routes.Route.Role;

import static io.javalin.apibuilder.ApiBuilder.*;

public class TripRoute {

    private final TripController tripController;

    public TripRoute(TripController tripController) {
        this.tripController = tripController;
    }

    public EndpointGroup getRoutes() {
        return () -> {
            get("/",        tripController::getAll, Role.ANYONE);
            get("/{id}",    tripController::getById, Role.ANYONE);
            post("/",       tripController::create,  Role.USER, Role.ADMIN);
            put("/{id}",    tripController::update,  Role.USER, Role.ADMIN);
            delete("/{id}", tripController::delete,  Role.USER);

            put("/{tripId}/guides/{guideId}", tripController::linkGuide, Role.USER);

            // US-6/US-5
            get("/{id}/packing/weight", tripController::getPackingWeight, Role.ANYONE);
            get("/guides/totalprice",   tripController::getTotalPricePerGuide, Role.ADMIN);
        };
    }
}
