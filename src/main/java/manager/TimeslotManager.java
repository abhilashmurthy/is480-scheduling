/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import constant.Response;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import model.Booking;
import model.Schedule;
import model.Timeslot;
import model.User;
import model.role.Faculty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Prakhar
 */
public class TimeslotManager {

    private static Logger logger = LoggerFactory.getLogger(TimeslotManager.class);

    public static Timeslot findById(EntityManager em, long id) {
        logger.trace("Getting timeslot based on id.");
        Timeslot timeslot = null;
		boolean justHere = false;
        try {
            if (!em.getTransaction().isActive()) {
				justHere = true;
				em.getTransaction().begin();
			}
            Query q = em.createQuery("select t from Timeslot t where t.id = :id")
                    .setParameter("id", id);
            timeslot = (Timeslot) q.getSingleResult();
            if (justHere) em.getTransaction().commit();
            return timeslot;
        } catch (Exception e) {
            logger.error("Database Operation Error");
            em.getTransaction().rollback();
        }
        return null;
    }
    
    public static List<Timeslot> findBySchedule(EntityManager em, Schedule schedule) {
        logger.trace("Getting timeslot based on Schedule ID: " + schedule);
        List<Timeslot> timeslots;
        boolean justHere = false;
        try {
            if (!em.getTransaction().isActive()) {
				em.getTransaction().begin();
                justHere = true;
            }
            Query q = em.createQuery("select t from Timeslot t where t.schedule = :schedule")
                    .setParameter("schedule", schedule);
            timeslots = (List<Timeslot>) q.getResultList();
            if (justHere) em.getTransaction().commit();
            return timeslots;
        } catch (Exception e) {
            logger.error("Database Operation Error");
            logger.error(e.getMessage());
            em.getTransaction().rollback();
        }
        return null;
    }

    public static boolean saveTimeslots(EntityManager em, Set<Timeslot> timeslots, EntityTransaction transaction) {
        logger.trace("Saving timeslots starting from: " + timeslots);
		boolean justHere = false;
        try {
			if (!em.getTransaction().isActive()) {
				justHere = true;
				em.getTransaction().begin();
			}
            for (Timeslot t : timeslots) {
                em.merge(t);
            }
            logger.trace("All timeslots have been saved");
			if (justHere) em.getTransaction().commit();
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
    
    public static void delete(EntityManager em, Timeslot timeslot) {
        logger.trace("Deleting timeslot: " + timeslot); //TODO: Optional attendees for bookings
		//Finding all faculty that marked this timeslot as unavailable
		Query findFaculty = em.createQuery("SELECT f from Faculty f WHERE :timeslot MEMBER OF f.unavailableTimeslots")
				.setParameter("timeslot", timeslot);
		List<Faculty> faculty = findFaculty.getResultList();
		for (Faculty f : faculty) { //Removing this timeslot from these faculty's availibility
			f.getUnavailableTimeslots().remove(timeslot);
		}
		//Finding all bookings related to this timeslot for removal
		Query findBookings = em.createQuery("SELECT b from Booking b WHERE b.timeslot = :timeslot").setParameter("timeslot", timeslot);
		List<Booking> bookings = findBookings.getResultList();
		for (Booking b : bookings) {
			b.setRequiredAttendees(null);
		}
		em.flush(); //Forcing write to database
        Query removeBookings = em.createQuery("DELETE FROM Booking b WHERE b.timeslot = :timeslot").setParameter("timeslot", timeslot);
		removeBookings.executeUpdate();
		em.remove(timeslot);
    }
     
     public static Timeslot getByTimestampAndSchedule(EntityManager em, Timestamp ts, Schedule s) {
        logger.trace("Getting Timeslot by Timestamp: " + ts + " and schedule: " + s);
        Timeslot timeslot = null;
		boolean justHere = false;
        try {
			if (!em.getTransaction().isActive()) {
				em.getTransaction().begin();
				justHere = true;
			}
            Query q = em.createQuery("select o from Timeslot o where o.startTime = :startTime and o.schedule = :schedule")
                    .setParameter("startTime", ts)
                    .setParameter("schedule", s);
            timeslot = (Timeslot) q.getSingleResult();
			if (justHere) em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Database Operation Error: " + e.getMessage());
            logger.trace(e.getMessage());
            em.getTransaction().rollback();
        }
        return timeslot;
    }
    
	 public static LinkedHashSet<String> getUniqueDatesForSchedule(EntityManager em, Schedule s) {
		 LinkedHashSet<String> uniqueDates = new LinkedHashSet<String>();
		 Query q = em.createQuery("select t from Timeslot t where t.schedule = :schedule order by t.startTime")
				 .setParameter("schedule", s);
		 List<Timeslot> slots = q.getResultList();
		 for (Timeslot t : slots) {
			 Timestamp startTime = t.getStartTime();
			 String dateStamp = startTime.toString().split(" ") [0];
			 uniqueDates.add(dateStamp);
		 }
		 return uniqueDates;
	 }
}
