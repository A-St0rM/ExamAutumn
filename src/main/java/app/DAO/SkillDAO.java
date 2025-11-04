package app.DAO;

import app.entities.Skill;
import app.exceptions.DatabaseException;
import app.exceptions.EntityNotFoundException;
import app.exceptions.ValidationException;
import jakarta.persistence.*;

import java.util.List;

public class SkillDAO implements IDAO<Skill, Integer> {  // Changed Long to Integer

    private final EntityManagerFactory emf;

    public SkillDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public List<Skill> getAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT s FROM Skill s", Skill.class).getResultList();
        } catch (PersistenceException e) {
            throw new DatabaseException("Failed to fetch skills");
        } finally {
            em.close();
        }
    }

    @Override
    public Skill getById(Integer id) {  // Changed Long to Integer
        EntityManager em = emf.createEntityManager();
        try {
            Skill s = em.find(Skill.class, id);
            if (s == null) {
                throw new EntityNotFoundException("Skill with id=" + id + " not found");
            }
            return s;
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid id for skill lookup");
        } catch (PersistenceException e) {
            throw new DatabaseException("Failed to fetch skill by id");
        } finally {
            em.close();
        }
    }

    @Override
    public Skill create(Skill skill) {
        if (skill == null) throw new ValidationException("Skill payload is null");

        // Simple validation
        if (skill.getName() == null || skill.getName().isBlank()) {
            throw new ValidationException("Skill name is required");
        }

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(skill);
            tx.commit();
            return skill;
        } catch (EntityExistsException e) {
            safeRollback(tx);
            throw new DatabaseException("Skill already exists");
        } catch (RollbackException e) {
            safeRollback(tx);
            throw new DatabaseException("Could not create skill (constraint violation?)");
        } catch (PersistenceException e) {
            safeRollback(tx);
            throw new DatabaseException("Failed to create skill");
        } finally {
            em.close();
        }
    }

    @Override
    public Skill update(Skill skill) {
        if (skill == null || skill.getId() == null)
            throw new ValidationException("Skill id is required for update");

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Skill existing = em.find(Skill.class, skill.getId());
            if (existing == null) {
                throw new EntityNotFoundException("Skill with id=" + skill.getId() + " not found");
            }
            tx.begin();
            Skill merged = em.merge(skill);
            tx.commit();
            return merged;
        } catch (RollbackException e) {
            safeRollback(tx);
            throw new DatabaseException("Could not update skill (constraint violation?)");
        } catch (PersistenceException e) {
            safeRollback(tx);
            throw new DatabaseException("Failed to update skill");
        } finally {
            em.close();
        }
    }

    @Override
    public boolean delete(Integer id) {  // Changed Long to Integer
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Skill s = em.find(Skill.class, id);
            if (s == null) {
                throw new EntityNotFoundException("Skill with id=" + id + " not found");
            }
            tx.begin();
            em.remove(s);
            tx.commit();
            return true;
        } catch (RollbackException e) {
            safeRollback(tx);
            throw new DatabaseException("Could not delete skill");
        } catch (PersistenceException e) {
            safeRollback(tx);
            throw new DatabaseException("Failed to delete skill");
        } finally {
            em.close();
        }
    }

    private void safeRollback(EntityTransaction tx) {
        if (tx != null && tx.isActive()) {
            try { tx.rollback(); } catch (Exception ignored) {}
        }
    }
}
