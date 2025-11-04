package app.DAO;

import app.entities.Candidate;
import app.entities.Skill;
import app.exceptions.DatabaseException;
import app.exceptions.EntityNotFoundException;
import app.exceptions.ValidationException;
import jakarta.persistence.*;

import java.util.List;
import java.util.Set;

public class CandidateDAO implements IDAO<Candidate, Integer> {  // Changed Long to Integer

    private final EntityManagerFactory emf;

    public CandidateDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public List<Candidate> getAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Candidate c", Candidate.class).getResultList();
        } catch (PersistenceException e) {
            throw new DatabaseException("Failed to fetch candidates");
        } finally {
            em.close();
        }
    }

    @Override
    public Candidate getById(Integer id) {  // Changed Long to Integer
        EntityManager em = emf.createEntityManager();
        try {
            Candidate c = em.find(Candidate.class, id);
            if (c == null) {
                throw new EntityNotFoundException("Candidate with id=" + id + " not found");
            }
            return c;
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid id for candidate lookup");
        } catch (PersistenceException e) {
            throw new DatabaseException("Failed to fetch candidate by id");
        } finally {
            em.close();
        }
    }

    @Override
    public Candidate create(Candidate candidate) {
        if (candidate == null) throw new ValidationException("Candidate payload is null");

        // Simple validation
        if (candidate.getName() == null || candidate.getName().isBlank()) {
            throw new ValidationException("Candidate name is required");
        }

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(candidate);
            tx.commit();
            return candidate;
        } catch (EntityExistsException e) {
            safeRollback(tx);
            throw new DatabaseException("Candidate already exists");
        } catch (RollbackException e) {
            safeRollback(tx);
            throw new DatabaseException("Could not create candidate (constraint violation?)");
        } catch (PersistenceException e) {
            safeRollback(tx);
            throw new DatabaseException("Failed to create candidate");
        } finally {
            em.close();
        }
    }

    @Override
    public Candidate update(Candidate candidate) {
        if (candidate == null || candidate.getId() == null)
            throw new ValidationException("Candidate id is required for update");

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Candidate existing = em.find(Candidate.class, candidate.getId());
            if (existing == null) {
                throw new EntityNotFoundException("Candidate with id=" + candidate.getId() + " not found");
            }
            tx.begin();
            Candidate merged = em.merge(candidate);
            tx.commit();
            return merged;
        } catch (RollbackException e) {
            safeRollback(tx);
            throw new DatabaseException("Could not update candidate (constraint violation?)");
        } catch (PersistenceException e) {
            safeRollback(tx);
            throw new DatabaseException("Failed to update candidate");
        } finally {
            em.close();
        }
    }

    @Override
    public boolean delete(Integer id) {  // Changed Long to Integer
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Candidate c = em.find(Candidate.class, id);
            if (c == null) {
                throw new EntityNotFoundException("Candidate with id=" + id + " not found");
            }
            tx.begin();
            em.remove(c);
            tx.commit();
            return true;
        } catch (RollbackException e) {
            safeRollback(tx);
            throw new DatabaseException("Could not delete candidate");
        } catch (PersistenceException e) {
            safeRollback(tx);
            throw new DatabaseException("Failed to delete candidate");
        } finally {
            em.close();
        }
    }

    public void linkCandidateSkills(Integer candidateId, Set<Skill> skills) {  // Changed Long to Integer
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Candidate candidate = em.find(Candidate.class, candidateId);
            if (candidate == null) {
                throw new EntityNotFoundException("Candidate with id=" + candidateId + " not found");
            }

            candidate.setSkills(skills);

            tx.begin();
            em.merge(candidate);
            tx.commit();

        } catch (PersistenceException e) {
            safeRollback(tx);
            throw new DatabaseException("Failed to link skills to candidate");
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
