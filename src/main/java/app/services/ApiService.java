package app.services;

import app.exceptions.ApiException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiService {

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