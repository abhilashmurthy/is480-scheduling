/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import constant.BookingStatus;
import java.util.ArrayList;
import java.util.Arrays;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import model.Booking;
import model.Schedule;
import model.Team;
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
					+ " and b.timeslot.schedule = :schedule and b.status in (:status)");
			q.setParameter("team", team);
			q.setParameter("schedule", schedule);
			q.setParameter("status", Arrays.asList(BookingStatus.PENDING, BookingStatus.APPROVED));
			list = (ArrayList<Booking>) q.getResultList();
		} catch (Exception e) {
			logger.error("Error in getActiveByTeamAndSchedule()");
			logger.error(e.getMessage());
			return null;
		}
		
		return list;
	}
	
}
