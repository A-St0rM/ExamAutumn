package app.controllers;

import app.DAO.GuideDAO;
import app.DAO.TripDAO;
import app.DTO.GuideDTO;
import app.DTO.PackingListResponseDTO;
import app.DTO.TripDTO;
import app.Integrations.PackingApiClient;
import app.entities.Guide;
import app.entities.Trip;
import app.exceptions.ValidationException;
import app.mapper.GuideMapper;
import app.mapper.TripMapper;
import jakarta.persistence.EntityManagerFactory;
import io.javalin.http.Context;

import java.util.List;

import static io.javalin.http.HttpStatus.*;

public class TripController {

    private final TripDAO tripDAO;
    private final GuideDAO guideDAO;
    private final PackingApiClient packingClient; // NEW

    public TripController(EntityManagerFactory emf, PackingApiClient packingClient) {
        this.tripDAO = new TripDAO(emf);
        this.guideDAO = new GuideDAO(emf);
        this.packingClient = packingClient;
    }

    // GET /trips
    public void getAll(Context ctx) {
        String categoryParam = ctx.queryParam("category");
        List<Trip> entities;

        if (categoryParam == null || categoryParam.isBlank()) {
            entities = tripDAO.getAll();
        } else {
            try {
                app.enums.Category category = app.enums.Category.valueOf(categoryParam.toUpperCase());
                entities = tripDAO.getByCategory(category);
            } catch (IllegalArgumentException ex) {
                throw new app.exceptions.ValidationException(
                        "Invalid category. Allowed values: " + java.util.Arrays.toString(app.enums.Category.values())
                );
            }
        }

        List<TripDTO> dtos = entities.stream()
                .map(app.mapper.TripMapper::toDTO)
                .toList();

        ctx.status(io.javalin.http.HttpStatus.OK).json(dtos);
    }

    // GET /trips/{id}
    public void getById(Context ctx) {
        Integer id = parseId(ctx.pathParam("id"), "id");
        Trip entity = tripDAO.getById(id);

        var dto = app.mapper.TripMapper.toDTO(entity);

        // HENT PAKKELISTE FRA EKSTERN API
        String categoryLower = entity.getCategory().name().toLowerCase(); // enum -> lower
        PackingListResponseDTO resp = packingClient.fetchByCategory(categoryLower);
        dto.setPackingItems(resp.getItems());

        ctx.status(OK).json(dto);
    }

    // GET /trips/{id}/packing/weight
    public void getPackingWeight(Context ctx) {
        Integer id = parseId(ctx.pathParam("id"), "id");
        Trip entity = tripDAO.getById(id);

        String categoryLower = entity.getCategory().name().toLowerCase();
        PackingListResponseDTO resp = packingClient.fetchByCategory(categoryLower);

        int totalGrams = resp.getItems() == null ? 0 :
                resp.getItems().stream()
                        .mapToInt(i -> Math.max(0, i.getWeightInGrams()) * Math.max(1, i.getQuantity()))
                        .sum();

        double totalKg = totalGrams / 1000.0;

        ctx.status(OK).json(java.util.Map.of(
                "tripId", id,
                "category", categoryLower,
                "totalWeightGrams", totalGrams,
                "totalWeightKg", totalKg
        ));
    }

    // POST /trips
    public void create(Context ctx) {
        TripDTO dto = ctx.bodyValidator(TripDTO.class)
                .check(d -> d.getName() != null && !d.getName().isBlank(), "name is required")
                .check(d -> d.getCategory() != null, "category is required")
                .check(d -> d.getGuide() != null && d.getGuide().getId() != null, "guide.id is required")
                .get();

        // slå guide op (DAO), så vi kan bygge en Trip entity korrekt
        Guide guide = guideDAO.getById(dto.getGuide().getId());

        Trip entity = TripMapper.toEntity(dto, guide);
        Trip saved = tripDAO.create(entity);                // DAO håndterer tx + exceptions
        ctx.status(CREATED).json(TripMapper.toDTO(saved));
    }

    // PUT /trips/{id}
    public void update(Context ctx) {
        Integer id = parseId(ctx.pathParam("id"), "id");

        TripDTO dto = ctx.bodyValidator(TripDTO.class)
                .check(d -> d.getId() == null || d.getId().equals(id),
                        "Body id (if present) must match path id")
                .check(d -> d.getName() != null && !d.getName().isBlank(), "name is required")
                .check(d -> d.getCategory() != null, "category is required")
                .get();

        dto.setId(id); // lås id til path

        Trip existing = tripDAO.getById(id);                // 404 hvis ikke findes
        Guide guide = null;
        if (dto.getGuide() != null && dto.getGuide().getId() != null) {
            guide = guideDAO.getById(dto.getGuide().getId());
        }

        // opdater entity-felter
        TripMapper.updateEntity(dto, existing, guide);
        Trip saved = tripDAO.update(existing);
        ctx.status(OK).json(TripMapper.toDTO(saved));
    }

    // DELETE /trips/{id}
    public void delete(Context ctx) {
        Integer id = parseId(ctx.pathParam("id"), "id");
        // Din DAO kaster gerne EntityNotFoundException hvis du vil – ellers boolean
        boolean ok = tripDAO.delete(id);
        if (ok) ctx.status(NO_CONTENT);
        else    ctx.status(NOT_FOUND).json(error("ENTITY_NOT_FOUND", "Trip with id=" + id + " not found"));
    }

    // PUT /trips/{tripId}/guides/{guideId}
    public void linkGuide(Context ctx) {
        Integer tripId = parseId(ctx.pathParam("tripId"), "tripId");
        Integer guideId = parseId(ctx.pathParam("guideId"), "guideId");
        Trip updated = tripDAO.linkGuide(tripId, guideId);  // smider 404/valideringsfejl
        ctx.status(OK).json(TripMapper.toDTO(updated));
    }

    public void getTotalPricePerGuide(Context ctx) {
        List<Object[]> results = tripDAO.getTotalPricePerGuide();

        List<app.DTO.GuideTotalDTO> dtoList = results.stream()
                .map(r -> new app.DTO.GuideTotalDTO(
                        ((Number) r[0]).intValue(),
                        ((Number) r[1]).doubleValue()
                ))
                .toList();

        ctx.status(io.javalin.http.HttpStatus.OK).json(dtoList);
    }




    // --- helpers ---

    private Integer parseId(String raw, String name) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Path parameter '" + name + "' must be an integer");
        }
    }

    private static java.util.Map<String, String> error(String code, String message) {
        return java.util.Map.of("error", code, "message", message);
    }
}

