package app.routes;

import app.controllers.CandidateController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class CandidateRoute {

    private final CandidateController candidateController;

    public CandidateRoute(CandidateController candidateController) {
        this.candidateController = candidateController;
    }

    //TODO: add security
    public EndpointGroup getRoutes() {
        return () -> {
            get("/", candidateController::getAll);
            get("/{id}", candidateController::getById);
            get("/", candidateController::getByCategory);
            post("/", candidateController::create);
            put("/{id}", candidateController::update);
            delete("/{id}", candidateController::delete);

            put("/{candidateId}/skills/{skillId}", candidateController::linkSkillToCandidate);
        };
    }
}
