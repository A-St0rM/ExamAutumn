package app.services;

import app.DTO.SkillStatsResponse;
import app.exceptions.ApiException;
import app.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import app.DTO.SkillStatsDTO;


public class ApiService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new Utils().getObjectMapper();
    }

    public List<SkillStatsDTO> fetchSkillStats(List<String> slugs) {
        String slugsParam = String.join(",", slugs);
        String url = "https://apiprovider.cphbusinessapps.dk/api/v1/skills/stats?slugs=" + slugsParam;


        String responseJson = fetchFromApi(url);

        // Deserialisér JSON svar til en liste af SkillStatsDTO
        try {
            SkillStatsResponse skillStatsResponse = objectMapper.readValue(responseJson, SkillStatsResponse.class);
            return skillStatsResponse.getData();
        } catch (Exception e) {
            throw new ApiException(500, "Error parsing Skill Stats API response: " + e.getMessage());
        }
    }


    public String fetchFromApi(String Uri){
        // sende HTTP-forespørgsler (GET, POST osv.) til en server.
        HttpClient httpClient = HttpClient.newHttpClient();   //newHttpClient(): laver en klient med standard-indstillinger (fx bruger den systemets proxy og HTTP/2 hvis muligt).

        try {
            // Create a request
            HttpRequest request = HttpRequest.newBuilder() //starter en request-builder.
                    .uri(new URI(Uri)) //fortæller hvilken URL (endpoint) man vil kalde. new URI(Uri) laver ens String
                    .GET()  // definerer, at det er en GET-request.
                    .build(); // bygger selve request-objektet.

            // Send request (get weather data) -få JSON som en stor tekststreng.
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());   //“Læs hele svaret fra serveren som en String”.


            // Check if the request went well
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new ApiException(response.statusCode(), "Error in fetching");
            }
        } catch (Exception e){
            throw new ApiException(500, e.getMessage());
        }
    }
}