package app.DAO;

import app.entities.Guide;
import app.entities.Trip;
import app.exceptions.DatabaseException;
import app.exceptions.EntityNotFoundException;
import app.exceptions.ValidationException;
import jakarta.persistence.*;

import java.util.List;

public class TripDAO implements IDAO<Trip, Integer> {

    private final EntityManagerFactory emf;

    public TripDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public List<Trip> getAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT t FROM Trip t", Trip.class).getResultList();
        } catch (PersistenceException e) {
            throw new DatabaseException("Failed to fetch trips");
        } finally {
            em.close();
        }
    }

    @Override
    public Trip getById(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            Trip t = em.find(Trip.class, id);
            if (t == null) {
                throw new EntityNotFoundException("Trip with id=" + id + " not found");
            }
            return t;
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid id for trip lookup");
        } catch (PersistenceException e) {
            throw new DatabaseException("Failed to fetch trip by id");
        } finally {
            em.close();
        }
    }

    @Override
    public Trip create(Trip trip) {
        if (trip == null) throw new ValidationException("Trip payload is null");
        if (trip.getName() == null || trip.getName().isBlank())
            throw new ValidationException("Trip name is required");
        if (trip.getGuide() == null || trip.getGuide().getId() == null)
            throw new ValidationException("Trip must reference an existing guide (guideId required)");

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            // Ensure guide exists & attach managed reference
            Guide managedGuide = em.find(Guide.class, trip.getGuide().getId());
            if (managedGuide == null) {
                throw new EntityNotFoundException("Guide with id=" + trip.getGuide().getId() + " not found");
            }

            tx.begin();
            trip.setGuide(managedGuide); // attach managed entity
            em.persist(trip);
            tx.commit();
            return trip;
        } catch (RollbackException e) {
            safeRollback(tx);
            throw new DatabaseException("Could not create trip (constraint violation?)");
        } catch (PersistenceException e) {
            safeRollback(tx);
            throw new DatabaseException("Failed to create trip");
        } finally {
            em.close();
        }
    }

    @Override
    public Trip update(Trip trip) {
        if (trip == null || trip.getId() == null)
            throw new ValidationException("Trip id is required for update");

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Trip existing = em.find(Trip.class, trip.getId());
            if (existing == null) {
                throw new EntityNotFoundException("Trip with id=" + trip.getId() + " not found");
            }

            // If a guide is provided, verify it exists and attach it
            Guide targetGuide = null;
            if (trip.getGuide() != null) {
                Integer gid = trip.getGuide().getId();
                if (gid == null) throw new ValidationException("guideId required when setting guide");
                targetGuide = em.find(Guide.class, gid);
                if (targetGuide == null) {
                    throw new EntityNotFoundException("Guide with id=" + gid + " not found");
                }
            }

            tx.begin();
            existing.setName(trip.getName());
            existing.setStartDate(trip.getStartDate());
            existing.setEndDate(trip.getEndDate());
            existing.setLocationCoordinates(trip.getLocationCoordinates());
            existing.setPrice(trip.getPrice());
            existing.setCategory(trip.getCategory());
            if (targetGuide != null) {
                existing.setGuide(targetGuide);
            }
            Trip merged = em.merge(existing);
            tx.commit();
            return merged;
        } catch (RollbackException e) {
            safeRollback(tx);
            throw new DatabaseException("Could not update trip (constraint violation?)");
        } catch (PersistenceException e) {
            safeRollback(tx);
            throw new DatabaseException("Failed to update trip");
        } finally {
            em.close();
        }
    }

    @Override
    public boolean delete(Integer id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Trip t = em.find(Trip.class, id);
            if (t == null) {
                throw new EntityNotFoundException("Trip with id=" + id + " not found");
            }
            tx.begin();
            em.remove(t);
            tx.commit();
            return true;
        } catch (RollbackException e) {
            safeRollback(tx);
            throw new DatabaseException("Could not delete trip");
        } catch (PersistenceException e) {
            safeRollback(tx);
            throw new DatabaseException("Failed to delete trip");
        } finally {
            em.close();
        }
    }

    /** Extra acceptance: link an existing Trip to an existing Guide */
    public Trip linkGuide(Integer tripId, Integer guideId) {
        if (tripId == null || guideId == null)
            throw new ValidationException("tripId and guideId are required");

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Trip t = em.find(Trip.class, tripId);
            if (t == null) throw new EntityNotFoundException("Trip with id=" + tripId + " not found");
            Guide g = em.find(Guide.class, guideId);
            if (g == null) throw new EntityNotFoundException("Guide with id=" + guideId + " not found");

            tx.begin();
            t.setGuide(g);
            Trip merged = em.merge(t);
            tx.commit();
            return merged;
        } catch (RollbackException e) {
            safeRollback(tx);
            throw new DatabaseException("Could not link guide to trip");
        } catch (PersistenceException e) {
            safeRollback(tx);
            throw new DatabaseException("Failed to link guide to trip");
        } finally {
            em.close();
        }
    }

    private void safeRollback(EntityTransaction tx) {
        if (tx != null && tx.isActive()) {
            try { tx.rollback(); } catch (Exception ignored) {}
        }
    }

    public List<Trip> getByCategory(app.enums.Category category) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT t FROM Trip t WHERE t.category = :cat", Trip.class)
                    .setParameter("cat", category)
                    .getResultList();
        } catch (PersistenceException e) {
            throw new app.exceptions.DatabaseException("Failed to fetch trips by category");
        } finally {
            em.close();
        }
    }

    public List<Object[]> getTotalPricePerGuide() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT t.guide.id, SUM(t.price) " +
                            "FROM Trip t " +
                            "GROUP BY t.guide.id", Object[].class
            ).getResultList();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new app.exceptions.DatabaseException("Failed to fetch total price per guide");
        } finally {
            em.close();
        }
    }

}
