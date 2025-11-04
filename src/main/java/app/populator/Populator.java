package app.populator;

import app.config.HibernateConfig;
import app.entities.Candidate;
import app.entities.Skill;
import app.enums.Category;
import jakarta.persistence.*;
import java.util.Set;

public class Populator {

    public static void populate() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Create Skills
            Skill skill1 = new Skill("Java", "Programming language", Category.PROG_LANG);
            Skill skill2 = new Skill("Docker", "Containerization technology", Category.DEVOPS);
            Skill skill3 = new Skill("SQL", "Database querying", Category.DB);
            Skill skill4 = new Skill("Project Management", "Managing projects", Category.FRONTEND);

            // Persist Skills
            em.persist(skill1);
            em.persist(skill2);
            em.persist(skill3);
            em.persist(skill4);

            // Create Candidates
            Candidate candidate1 = new Candidate("Alice Johnson", "123456789", "B.Sc. in Computer Science");
            Candidate candidate2 = new Candidate("Bob Smith", "987654321", "M.Sc. in Information Systems");

            // Assign Skills to Candidates
            candidate1.setSkills(Set.of(skill1, skill2));
            candidate2.setSkills(Set.of(skill3, skill4));

            // Persist Candidates
            em.persist(candidate1);
            em.persist(candidate2);

            em.getTransaction().commit();

            System.out.println("✅ Sample data inserted.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
