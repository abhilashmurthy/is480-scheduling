/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import model.Timeslot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class TimeslotManager {

    private static Logger logger = LoggerFactory.getLogger(UserManager.class);

    public static Timeslot findById(EntityManager em, long id) {
        logger.info("Getting timeslot based on id.");
        Timeslot timeslot = null;
        try {
            em.getTransaction().begin();
            Query q = em.createQuery("select t from Timeslot t where t.id = :id")
                    .setParameter("id", id);
            timeslot = (Timeslot) q.getSingleResult();
            em.getTransaction().commit();
            return timeslot;
        } catch (Exception e) {
            logger.error("Database Operation Error");
            em.getTransaction().rollback();
        }
        return null;
    }
	
	public static boolean updateTimeslotStatus (EntityManager em, List<Timeslot> timeslotsToUpdate, EntityTransaction transaction) {
		logger.info("Updating timeslot status");
        try {
            transaction.begin();
			for (Timeslot timeslot: timeslotsToUpdate) {
				em.persist(timeslot);
			}
			transaction.commit();
            return true;
        } catch (PersistenceException ex) {
            //Rolling back data transactions
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error making database call for update timeslot status");
            ex.printStackTrace();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return false;
    }
    
    public static boolean saveTimeslots(EntityManager em, Set<Timeslot> timeslots, EntityTransaction transaction) {
        logger.info("Saving timeslots starting from: " + timeslots);
        try {
            transaction = em.getTransaction();
            transaction.begin();
            for (Timeslot t : timeslots) {
                em.persist(t);
            }
            logger.debug("All timeslots have been saved");
            transaction.commit();
            return true;
        } catch (PersistenceException ex) {
            //Rolling back data transactions
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error making database call for update timeslot status");
            ex.printStackTrace();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return false;
    }
    
    public static Timeslot save(EntityManager em, Timeslot timeslot, EntityTransaction transaction) {
        logger.info("Saving timeslot: " + timeslot);
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.persist(timeslot);
            logger.debug("All timeslots have been saved");
            transaction.commit();
            return timeslot;
        } catch (PersistenceException ex) {
            //Rolling back data transactions
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error making database call for update timeslot status");
            ex.printStackTrace();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }
}
