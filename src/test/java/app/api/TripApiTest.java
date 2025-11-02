    // src/test/java/app/api/TripApiTest.java
    package app.api;

    import app.config.ApplicationConfig;
    import app.config.HibernateConfig;
    import app.FakePackingApiClient;
    import app.populator.TripPopulator;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
    import io.restassured.RestAssured;
    import jakarta.persistence.EntityManagerFactory;
    import org.junit.jupiter.api.*;

    import java.util.Map;

    import static io.restassured.RestAssured.given;
    import static org.hamcrest.Matchers.*;

    class TripApiTest {

        private static EntityManagerFactory emf;
        private static ObjectMapper om;
        private static ApplicationConfig appConfig;

        private Integer guideId;
        private Integer tripIdBeach;
        private Integer tripIdForest;
        private static String token;

        @BeforeAll
        static void setupAll() {
            HibernateConfig.setTest(true);
            emf = HibernateConfig.getEntityManagerFactoryForTest();
            om = new ObjectMapper().registerModule(new JavaTimeModule());

            // Start server på test-port med FAKE packing client (ingen netværk)
            var fakePacking = new FakePackingApiClient(om);
            appConfig = ApplicationConfig.startServer(7778, fakePacking);

            RestAssured.baseURI = "http://localhost:7778/api/v1";

            // Opret testbruger og login for at få JWT
            given().contentType("application/json")
                    .body("{\"username\":\"u1\",\"password\":\"p1\"}")
                    .when().post("/auth/register")
                    .then().statusCode(anyOf(is(200), is(201)));

            token = given()
                    .contentType("application/json")
                    .body("{\"username\":\"u1\",\"password\":\"p1\"}")
                    .when().post("/auth/login")
                    .then().statusCode(200)
                    .extract()
                    .jsonPath()
                    .getString("token");

        }

        @AfterAll
        static void tearDownAll() {
            HibernateConfig.setTest(false);
            // Hvis du har reference til Javalin-app, så stop den her
            // ApplicationConfig.stopServer(app);
        }

        @BeforeEach
        void seedDb() {
            Map<String, Integer> ids = TripPopulator.seedData(emf);
            guideId = ids.get("guideId");
            tripIdBeach = ids.get("tripBeachId");
            tripIdForest = ids.get("tripForestId");
        }

        @Test
        void getAll_and_filterByCategory() {
            // all
            given()
                    .when().get("/trip")
                    .then()
                    .statusCode(200)
                    .body("name", hasItems("Sunny Beach", "Deep Forest"));
            // filter
            given()
                    .when().get("/trip?category=beach")
                    .then()
                    .statusCode(200)
                    .body("name", everyItem(equalTo("Sunny Beach")))
                    .body("size()", equalTo(1));
        }

        @Test
        void getById_includesPackingItems() {
            given()
                    .when().get("/trip/" + tripIdBeach)
                    .then()
                    .statusCode(200)
                    .body("name", equalTo("Sunny Beach"))
                    .body("packingItems", notNullValue())
                    .body("packingItems.size()", equalTo(2))
                    .body("packingItems.name", hasItems("Towel", "Sunscreen"));
        }

        @Test
        void packingWeight_returnsTotal() {
            given()
                    .when().get("/trip/" + tripIdBeach + "/packing/weight")
                    .then()
                    .statusCode(200)
                    .body("tripId", equalTo(tripIdBeach))
                    .body("totalWeightGrams", equalTo(800))
                    .body("totalWeightKg", equalTo(0.8f));
        }

        @Test
        void create_update_link_delete_flow() {
            String createJson = """
            {
              "name":"City Tour",
              "startDate":"2025-01-01",
              "endDate":"2025-01-03",
              "locationCoordinates":"55.7,12.5",
              "price":2500,
              "category":"CITY",
              "guide":{"id":%d}
            }
            """.formatted(guideId);

            int newId =
                    given()
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .body(createJson)
                            .when().post("/trip")
                            .then()
                            .statusCode(201)
                            .body("name", equalTo("City Tour"))
                            .extract().path("id");

            String updateJson = """
            {
              "id":%d,
              "name":"City Tour XL",
              "startDate":"2025-01-01",
              "endDate":"2025-01-04",
              "locationCoordinates":"55.7,12.5",
              "price":3000,
              "category":"CITY",
              "guide":{"id":%d}
            }
            """.formatted(newId, guideId);

            given()
                    .header("Authorization", "Bearer " + token)
                    .contentType("application/json")
                    .body(updateJson)
                    .when().put("/trip/" + newId)
                    .then()
                    .statusCode(200)
                    .body("name", equalTo("City Tour XL"))
                    .body("price", equalTo(3000.0F));

            given()
                    .header("Authorization", "Bearer " + token)
                    .when().put("/trip/" + newId + "/guides/" + guideId)
                    .then()
                    .statusCode(200)
                    .body("guide.id", equalTo(guideId));

            given()
                    .header("Authorization", "Bearer " + token)
                    .when().delete("/trip/" + newId)
                    .then()
                    .statusCode(204);
        }
    }
