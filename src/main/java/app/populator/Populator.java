package app.populator;

import app.config.HibernateConfig;
import app.entities.Guide;
import app.entities.Trip;
import app.enums.Category;
import jakarta.persistence.*;

import java.time.LocalDate;

public class Populator {
    public static void populate() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Guide g1 = new Guide(null, "Ali Hassan", "ali@example.com", "12345678", 5);
            Guide g2 = new Guide(null, "Sara Jensen", "sara@example.com", "87654321", 8);
            em.persist(g1);
            em.persist(g2);

            Trip t1 = new Trip();
            t1.setName("Beach Adventure");
            t1.setStartDate(LocalDate.of(2025, 7, 10));
            t1.setEndDate(LocalDate.of(2025, 7, 15));
            t1.setLocationCoordinates("55.6761N, 12.5683E");
            t1.setPrice(1500.0);
            t1.setCategory(Category.BEACH);
            t1.setGuide(g1);

            Trip t2 = new Trip();
            t2.setName("Forest Retreat");
            t2.setStartDate(LocalDate.of(2025, 8, 5));
            t2.setEndDate(LocalDate.of(2025, 8, 10));
            t2.setLocationCoordinates("61.123N, 8.123E");
            t2.setPrice(2500.0);
            t2.setCategory(Category.FOREST);
            t2.setGuide(g2);

            Trip t3 = new Trip();
            t3.setName("City Walk");
            t3.setStartDate(LocalDate.of(2025, 9, 1));
            t3.setEndDate(LocalDate.of(2025, 9, 3));
            t3.setLocationCoordinates("55.687N, 12.570E");
            t3.setPrice(1200.0);
            t3.setCategory(Category.CITY);
            t3.setGuide(g1);

            em.persist(t1);
            em.persist(t2);
            em.persist(t3);

            em.getTransaction().commit();
            System.out.println("✅ Sample data inserted.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
