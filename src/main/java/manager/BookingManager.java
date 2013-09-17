/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import constant.BookingStatus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import model.Booking;
import model.Schedule;
import model.Team;
import model.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author suresh
 */
public class BookingManager {
	private static Logger logger = LoggerFactory.getLogger(BookingManager.class);
	
	/**
	 * Get approved/pending bookings by a team in a schedule. Returned list should not be greater than 1.
	 * @param em
	 * @param team
	 * @param schedule
	 * @return 
	 */
	public static ArrayList<Booking> getActiveByTeamAndSchedule(EntityManager em, Team team, Schedule schedule) {
		logger.trace("Getting bookings by team and schedule");
		ArrayList<Booking> list;
		try {
			Query q = em.createQuery("select b from Booking b where b.team = :team"
					+ " and b.timeslot.schedule = :schedule and b.bookingStatus in (:bookingStatus)");
			q.setParameter("team", team);
			q.setParameter("schedule", schedule);
			q.setParameter("bookingStatus", Arrays.asList(BookingStatus.PENDING, BookingStatus.APPROVED));
			list = (ArrayList<Booking>) q.getResultList();
		} catch (Exception e) {
			logger.error("Error in getActiveByTeamAndSchedule()");
			logger.error(e.getMessage());
			return null;
		}
		
		return list;
	}
	
	public static boolean updateBookings(EntityManager em, List<Booking> bookingsToUpdate, EntityTransaction transaction) {
        logger.trace("Updating bookings");
        try {
            transaction.begin();
            for (Booking booking : bookingsToUpdate) {
                em.persist(booking);
            }
            transaction.commit();
            return true;
        } catch (PersistenceException ex) {
            //Rolling back data transactions
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error making database call for update bookings");
            ex.printStackTrace();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return false;
    }
	
	/* To get all bookings for active term */
	public static ArrayList<Booking> getBookingsByTerm (EntityManager em, Term term) {
		logger.trace("Getting all bookings by active term");
		ArrayList<Booking> list = null;
		try {
			Query q = em.createQuery("select b from Booking b where b.timeslot.schedule.milestone.term = :term");
			q.setParameter("term", term);
			list = (ArrayList<Booking>) q.getResultList();
		} catch (Exception e) {
			logger.error("Error in getBookingsByTerm()");
			logger.error(e.getMessage());
			return null;
		}
		return list;
	}
	
}
