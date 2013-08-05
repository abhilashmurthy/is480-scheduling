/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import javax.persistence.EntityManager;
import manager.ScheduleManager;
import manager.TermManager;
import model.Schedule;
import model.Term;

/**
 * Utility class to put miscellaneous code
 * @author suresh
 */
public class MiscUtil {
	/**
	 * Name of the Persistence Unit used application-wide
	 */
	public static final String PERSISTENCE_UNIT = "scheduler";
	
	/**
	 * Boolean variable to check if the system is currently running in development mode
	 */
	public static final boolean DEV_MODE = true;
	
	public static Term getActiveTerm(EntityManager em) {
		return TermManager.findByYearAndSemester(em, 2013, "Term 1");
	}
	
	public static Schedule getActiveSchedule(EntityManager em) {
		Term activeTerm = getActiveTerm(em);
		return ScheduleManager.findActiveByTerm(em, activeTerm);
	}
	
	// Gives the schedule based on the timeslot date (Each timeslot is part of 1 schedule)
//	public static Schedule getScheduleByTimeslot (EntityManager em, Timeslot timeslot) {
//		Timestamp timeslotTime = timeslot.getStartTime();
//		List<Schedule> allSchedules = ScheduleManager.getAllSchedules(em);
//		if (allSchedules.size() > 0) {
//			for (Schedule schedule: allSchedules) {
//				Timestamp startDate = schedule.getStartDate();
//				Timestamp endDate = schedule.getEndDate();
//				//Checking whether the timeslot date falls between the schedule dates
//				if (timeslotTime.after(startDate) && timeslotTime.before(endDate)) {
//					return schedule;
//				}
//			}
//			return null;
//		}
//		return null;
//	}
}
