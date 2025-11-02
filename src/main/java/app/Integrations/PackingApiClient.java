package app.Integrations;

import app.DTO.PackingListResponseDTO;
import app.services.ApiService;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PackingApiClient {
    private final ApiService apiService;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public PackingApiClient(ApiService apiService, ObjectMapper objectMapper, String baseUrl) {
        this.apiService = apiService;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
    }

    public PackingListResponseDTO fetchByCategory(String categoryLower) {
        String uri = baseUrl + "/packinglist/" + categoryLower;
        String json = apiService.fetchFromApi(uri);
        try {
            return objectMapper.readValue(json, PackingListResponseDTO.class);
        } catch (Exception e) {
            throw new app.exceptions.ExternalServiceException("Failed to parse Packing API response", e);
        }
    }
}

