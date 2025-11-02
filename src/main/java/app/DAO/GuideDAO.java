package app.DAO;

import app.entities.Guide;
import app.exceptions.DatabaseException;
import app.exceptions.EntityNotFoundException;
import app.exceptions.ValidationException;
import jakarta.persistence.*;

import java.util.List;

public class GuideDAO implements IDAO<Guide, Integer> {

    private final EntityManagerFactory emf;

    public GuideDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public List<Guide> getAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT g FROM Guide g", Guide.class).getResultList();
        } catch (PersistenceException e) {
            throw new DatabaseException("Failed to fetch guides");
        } finally {
            em.close();
        }
    }

    @Override
    public Guide getById(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            Guide g = em.find(Guide.class, id);
            if (g == null) {
                throw new EntityNotFoundException("Guide with id=" + id + " not found");
            }
            return g;
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid id for guide lookup");
        } catch (PersistenceException e) {
            throw new DatabaseException("Failed to fetch guide by id");
        } finally {
            em.close();
        }
    }

    @Override
    public Guide create(Guide guide) {
        if (guide == null) throw new ValidationException("Guide payload is null");
        // simple validation example
        if (guide.getName() == null || guide.getName().isBlank())
            throw new ValidationException("Guide name is required");
        if (guide.getEmail() == null || guide.getEmail().isBlank())
            throw new ValidationException("Guide email is required");

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(guide);
            tx.commit();
            return guide;
        } catch (EntityExistsException e) {
            safeRollback(tx);
            throw new DatabaseException("Guide already exists");
        } catch (RollbackException e) { // constraint violations etc.
            safeRollback(tx);
            throw new DatabaseException("Could not create guide (constraint violation?)");
        } catch (PersistenceException e) {
            safeRollback(tx);
            throw new DatabaseException("Failed to create guide");
        } finally {
            em.close();
        }
    }

    @Override
    public Guide update(Guide guide) {
        if (guide == null || guide.getId() == null)
            throw new ValidationException("Guide id is required for update");

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Guide existing = em.find(Guide.class, guide.getId());
            if (existing == null) {
                throw new EntityNotFoundException("Guide with id=" + guide.getId() + " not found");
            }
            tx.begin();
            Guide merged = em.merge(guide);
            tx.commit();
            return merged;
        } catch (RollbackException e) {
            safeRollback(tx);
            throw new DatabaseException("Could not update guide (constraint violation?)");
        } catch (PersistenceException e) {
            safeRollback(tx);
            throw new DatabaseException("Failed to update guide");
        } finally {
            em.close();
        }
    }

    @Override
    public boolean delete(Integer id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Guide g = em.find(Guide.class, id);
            if (g == null) {
                throw new EntityNotFoundException("Guide with id=" + id + " not found");
            }
            tx.begin();
            em.remove(g);
            tx.commit();
            return true;
        } catch (RollbackException e) {
            safeRollback(tx);
            throw new DatabaseException("Could not delete guide");
        } catch (PersistenceException e) {
            safeRollback(tx);
            throw new DatabaseException("Failed to delete guide");
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
