package app.api;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.populator.CandidatePopulator;
import app.services.ApiService;
import app.DTO.SkillStatsDTO;
import io.restassured.RestAssured;
import io.javalin.Javalin;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class CandidateApiTest {

    private static EntityManagerFactory emf;
    private static String token;

    private Integer candidateId1;
    private Integer candidateId2;

    @BeforeAll
    static void setupAll() {
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactoryForTest();

        // Mock API service for the test to avoid real HTTP calls
        ApiService mockApiService = new ApiService() {
            @Override
            public List<SkillStatsDTO> fetchSkillStats(List<String> slugs) {
                return slugs.stream().map(slug -> {
                    SkillStatsDTO dto = new SkillStatsDTO();
                    dto.setSlug(slug);
                    dto.setPopularityScore(80);
                    dto.setAverageSalary(100000);
                    return dto;
                }).toList();
            }
        };


        Javalin app = ApplicationConfig.startServer(7779, mockApiService);

        RestAssured.baseURI = "http://localhost:7779/api/v1";

        // Register and login for JWT token
        given().contentType("application/json")
                .body("{\"username\":\"u1\",\"password\":\"p1\"}")
                .when().post("/auth/register")
                .then().statusCode(201);

        token = given()
                .contentType("application/json")
                .body("{\"username\":\"u1\",\"password\":\"p1\"}")
                .when().post("/auth/login")
                .then().statusCode(200)
                .extract()
                .jsonPath()
                .getString("token");
    }

    @BeforeEach
    void seedDb() {
        Map<String, Integer> ids = CandidatePopulator.seedData(emf);
        candidateId1 = ids.get("candidate1");
        candidateId2 = ids.get("candidate2");
    }

    @Test
    void getAllCandidates_returnsAll() {
        given()
                .when().get("/candidate")
                .then()
                .statusCode(200)
                .body("name", hasItems("Alice", "Bob"));
    }

    @Test
    void getCandidateById_includesEnrichment() {
        given()
                .when().get("/candidate/" + candidateId1)
                .then()
                .statusCode(200)
                .body("id", equalTo(candidateId1))
                .body("skills", notNullValue())
                .body("skills[0].popularityScore", equalTo(80))
                .body("skills[0].averageSalary", equalTo(100000));
    }

    @Test
    void getCandidatesByCategory_filtersCorrectly() {
        given()
                .when().get("/candidate?category=PROG_LANG")
                .then()
                .statusCode(200)
                .body("findAll { it.skills.any { it.category == 'PROG_LANG' } }", not(empty()));
    }

    @Test
    void getTopCandidateByPopularity() {
        given()
                .when().get("candidate/reports/candidates/top-by-popularity")
                .then()
                .statusCode(200)
                .body("candidateId", notNullValue())
                .body("averagePopularityScore", greaterThan(0f));
    }

    @Test
    void create_update_deleteFlow() {
        String createJson = """
            {
              "name":"Charlie",
              "phone":"12345678",
              "educationBackground":"Computer Science"
            }
            """;

        int newId =
                given()
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .body(createJson)
                        .when().post("/candidate")
                        .then()
                        .statusCode(201)
                        .body("name", equalTo("Charlie"))
                        .extract().path("id");

        String updateJson = """
            {
              "id":%d,
              "name":"Charlie Updated",
              "phone":"87654321",
              "educationBackground":"CS Updated"
            }
            """.formatted(newId);

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(updateJson)
                .when().put("/candidate/" + newId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Charlie Updated"))
                .body("phone", equalTo("87654321"));

        given()
                .header("Authorization", "Bearer " + token)
                .when().delete("/candidate/" + newId)
                .then()
                .statusCode(204);
    }

    @Test
    void linkSkillToCandidate_worksCorrectly() {

        int skillId = 1;

        given()
                .header("Authorization", "Bearer " + token)
                .when().put("/candidate/" + candidateId1 + "/skills/" + skillId)
                .then()
                .statusCode(204);


        given()
                .when().get("/candidate/" + candidateId1)
                .then()
                .statusCode(200)
                .body("skills.id", hasItem(skillId));
    }

    @Test
    void getCandidateById_invalidId_returns404() {
        int invalidId = 9999;

        given()
                .when().get("/candidate/" + invalidId)
                .then()
                .statusCode(404)
                .body("error", equalTo("ENTITY_NOT_FOUND"))
                .body("message", equalTo("Candidate with id=" + invalidId + " not found"));
    }
}
