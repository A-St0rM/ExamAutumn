package app.routes;

import app.controllers.CandidateController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class CandidateRoute {

    private final CandidateController candidateController;

    public CandidateRoute(CandidateController candidateController) {
        this.candidateController = candidateController;
    }

    public EndpointGroup getRoutes() {
        return () -> {
            get("/", candidateController::getAll);
            get("/{id}", candidateController::getById);
            post("/", candidateController::create);
            put("/{id}", candidateController::update);
            delete("/{id}", candidateController::delete);

            put("/{candidateId}/skills/{skillId}", candidateController::linkSkillToCandidate);
        };
    }
}
