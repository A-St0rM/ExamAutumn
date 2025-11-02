package app.populator;

import app.DAO.GuideDAO;
import app.DAO.TripDAO;
import app.entities.Guide;
import app.entities.Trip;
import app.enums.Category;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class TripPopulator {

    /**
     * Seeder testdata i databasen.
     * @param emf EntityManagerFactory til test (HibernateConfig.getEntityManagerFactoryForTest())
     * @return Map med IDs for guide og trips
     */
    public static Map<String, Integer> seedData(EntityManagerFactory emf) {
        var guideDAO = new GuideDAO(emf);
        var tripDAO  = new TripDAO(emf);

        Guide guide = new Guide();
        guide.setName("Test Guide");
        guide.setEmail("guide@test.com");
        guide.setPhone("12345678");
        guide.setYearsOfExperience(5);
        guide = guideDAO.create(guide);


        Trip beachTrip = new Trip();
        beachTrip.setName("Sunny Beach");
        beachTrip.setStartDate(LocalDate.now());
        beachTrip.setEndDate(LocalDate.now().plusDays(3));
        beachTrip.setLocationCoordinates("55.6761,12.5683");
        beachTrip.setPrice(3000);
        beachTrip.setCategory(Category.BEACH);
        beachTrip.setGuide(guide);
        beachTrip = tripDAO.create(beachTrip);


        Trip forestTrip = new Trip();
        forestTrip.setName("Deep Forest");
        forestTrip.setStartDate(LocalDate.now().plusDays(5));
        forestTrip.setEndDate(LocalDate.now().plusDays(8));
        forestTrip.setLocationCoordinates("56.0000,11.0000");
        forestTrip.setPrice(4500);
        forestTrip.setCategory(Category.FOREST);
        forestTrip.setGuide(guide);
        forestTrip = tripDAO.create(forestTrip);


        Map<String, Integer> ids = new HashMap<>();
        ids.put("guideId", guide.getId());
        ids.put("tripBeachId", beachTrip.getId());
        ids.put("tripForestId", forestTrip.getId());

        return ids;
    }
}
