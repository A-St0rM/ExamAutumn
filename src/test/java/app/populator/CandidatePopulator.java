package app.populator;

import app.DAO.CandidateDAO;
import app.DAO.SkillDAO;
import app.entities.Candidate;
import app.entities.Skill;
import app.enums.Category;
import jakarta.persistence.EntityManagerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CandidatePopulator {

    /**
     * Seeder testdata i databasen.
     * @param emf EntityManagerFactory til test (HibernateConfig.getEntityManagerFactoryForTest())
     * @return Map med IDs for kandidater og skills
     */
    public static Map<String, Integer> seedData(EntityManagerFactory emf) {
        var candidateDAO = new CandidateDAO(emf);
        var skillDAO = new SkillDAO(emf);

        // Opret Skills
        Skill javaSkill = new Skill("Java", "Backend programming", Category.PROG_LANG);
        Skill springSkill = new Skill("Spring Boot", "Java framework", Category.FRAMEWORK);
        javaSkill = skillDAO.create(javaSkill);
        springSkill = skillDAO.create(springSkill);

        // Opret Kandidater
        Candidate alice = new Candidate("Alice", "11111111", "Computer Science");
        alice.setSkills(Set.of(javaSkill, springSkill));
        alice = candidateDAO.create(alice);

        Candidate bob = new Candidate("Bob", "22222222", "Software Engineering");
        bob.setSkills(Set.of(springSkill));
        bob = candidateDAO.create(bob);

        Map<String, Integer> ids = new HashMap<>();
        ids.put("candidate1", alice.getId());
        ids.put("candidate2", bob.getId());
        ids.put("skillJava", javaSkill.getId());
        ids.put("skillSpring", springSkill.getId());

        return ids;
    }
}
