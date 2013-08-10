/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import constant.Status;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import model.Schedule;
import model.Team;
import model.Timeslot;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class TimeslotManager {

    private static Logger logger = LoggerFactory.getLogger(TimeslotManager.class);

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
    
    public static List<Timeslot> findBySchedule(EntityManager em, Schedule schedule) {
        logger.info("Getting timeslot based on Schedule ID: " + schedule);
        List<Timeslot> timeslots;
        try {
            em.getTransaction().begin();
            Query q = em.createQuery("select t from Timeslot t where t.schedule = :schedule")
                    .setParameter("schedule", schedule);
            timeslots = (List<Timeslot>) q.getResultList();
            em.getTransaction().commit();
            return timeslots;
        } catch (Exception e) {
            logger.error("Database Operation Error");
            logger.error(e.getMessage());
            em.getTransaction().rollback();
        }
        return null;
    }

    public static boolean updateTimeslotStatus(EntityManager em, List<Timeslot> timeslotsToUpdate, EntityTransaction transaction) {
        logger.info("Updating timeslot status");
        try {
            transaction.begin();
            for (Timeslot timeslot : timeslotsToUpdate) {
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
                em.merge(t);
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
    
    public static boolean delete(EntityManager em, Timeslot timeslot, EntityTransaction transaction) {
        logger.info("Deleting timeslot: " + timeslot);
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.remove(timeslot);
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

    public static boolean deleteTimeslotBooking(EntityManager em, Timeslot ts) {
        EntityTransaction transaction = em.getTransaction();
        try {
            HashMap<User, Status> statusList = new HashMap<User, Status>();
            
            transaction.begin();
            String sQuery = "UPDATE Timeslot t SET t.statusList = :statusList, t.team = :team"
                   + "WHERE t.id = :id";
           
            Query query = em.createQuery(sQuery);

            query.setParameter("statusList", statusList);
            query.setParameter("team", null);
            query.setParameter("id", ts.getId());
            
            query.executeUpdate();
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
    
     public static List<Timeslot> getAllTimeslots(EntityManager em) {
        logger.info("Getting all timeslots");
        List<Timeslot> result = null;
        try {
            em.getTransaction().begin();
            Query q = em.createQuery("select o from Timeslot o");
            result = q.getResultList();
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Database Operation Error");
            em.getTransaction().rollback();
        }
        return result;
    }
    

    
    
}
