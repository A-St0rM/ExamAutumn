package app;

import app.DTO.PackingItemDTO;
import app.DTO.PackingListResponseDTO;
import app.Integrations.PackingApiClient;
import app.services.ApiService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.ZonedDateTime;
import java.util.List;

public class FakePackingApiClient extends PackingApiClient {
    public FakePackingApiClient(ObjectMapper om) {
        super(new ApiService(), om, "http://fake");
    }
    @Override
    public PackingListResponseDTO fetchByCategory(String categoryLower) {
        var towel = new PackingItemDTO();
        towel.setName("Towel");
        towel.setWeightInGrams(300);
        towel.setQuantity(2);
        towel.setDescription("Test towel");
        towel.setCategory(categoryLower);
        towel.setCreatedAt(ZonedDateTime.now());
        towel.setUpdatedAt(ZonedDateTime.now());
        towel.setBuyingOptions(List.of());

        var sunscreen = new PackingItemDTO();
        sunscreen.setName("Sunscreen");
        sunscreen.setWeightInGrams(200);
        sunscreen.setQuantity(1);
        sunscreen.setDescription("Test sunscreen");
        sunscreen.setCategory(categoryLower);
        sunscreen.setCreatedAt(ZonedDateTime.now());
        sunscreen.setUpdatedAt(ZonedDateTime.now());
        sunscreen.setBuyingOptions(List.of());

        var resp = new PackingListResponseDTO();
        resp.setItems(List.of(towel, sunscreen)); // total 800g
        return resp;
    }
}

