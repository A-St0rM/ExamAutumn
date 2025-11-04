package app.controllers;

import app.services.CandidateService;
import app.DTO.CandidateDTO;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    // GET /candidates
    public void getAll(Context ctx) {
        var candidates = candidateService.getAllCandidates();
        ctx.status(HttpStatus.OK).json(candidates);
    }

    // GET /candidates/{id}
    public void getById(Context ctx) {
        Integer id = Integer.valueOf(ctx.pathParam("id"));
        CandidateDTO candidate = candidateService.getCandidateById(id);
        if (candidate != null) {
            ctx.status(HttpStatus.OK).json(candidate);
        } else {
            ctx.status(HttpStatus.NOT_FOUND).result("Candidate not found");
        }
    }

    // POST /candidates
    public void create(Context ctx) {
        CandidateDTO candidateDTO = ctx.bodyValidator(CandidateDTO.class)
                .check(dto -> dto.getName() != null && !dto.getName().isBlank(), "Name is required")
                .check(dto -> dto.getPhone() != null && !dto.getPhone().isBlank(), "Phone is required")
                .check(dto -> dto.getEducationBackground() != null && !dto.getEducationBackground().isBlank(), "Education background is required")
                .get();

        CandidateDTO createdCandidate = candidateService.createCandidate(candidateDTO);
        ctx.status(HttpStatus.CREATED).json(createdCandidate);
    }

    // PUT /candidates/{id}
    public void update(Context ctx) {
        Integer id = Integer.valueOf(ctx.pathParam("id"));
        CandidateDTO candidateDTO = ctx.bodyValidator(CandidateDTO.class)
                .check(dto -> dto.getId() == null || dto.getId().equals(id), "Body id must match path id")
                .get();

        candidateDTO.setId(id);
        CandidateDTO updatedCandidate = candidateService.updateCandidate(candidateDTO);
        ctx.status(HttpStatus.OK).json(updatedCandidate);
    }

    // DELETE /candidates/{id}
    public void delete(Context ctx) {
        Integer id = Integer.valueOf(ctx.pathParam("id"));
        boolean deleted = candidateService.deleteCandidate(id);
        if (deleted) {
            ctx.status(HttpStatus.NO_CONTENT);
        } else {
            ctx.status(HttpStatus.NOT_FOUND).result("Candidate not found");
        }
    }

    // PUT /candidates/{candidateId}/skills/{skillId}
    public void linkSkillToCandidate(Context ctx) {
        Integer candidateId = Integer.valueOf(ctx.pathParam("candidateId"));
        Integer skillId = Integer.valueOf(ctx.pathParam("skillId"));
        boolean linked = candidateService.linkSkillToCandidate(candidateId, skillId);
        if (linked) {
            ctx.status(HttpStatus.NO_CONTENT);
        } else {
            ctx.status(HttpStatus.NOT_FOUND).result("Candidate or Skill not found");
        }
    }
}
