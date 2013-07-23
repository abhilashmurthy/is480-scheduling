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
	
	public static Term getActiveTerm(EntityManager em) {
		return TermManager.findByYearAndSemester(em, 2013, "Term 1");
	}
	
	public static Schedule getActiveSchedule(EntityManager em) {
		Term activeTerm = getActiveTerm(em);
		return ScheduleManager.findActiveByTerm(em, activeTerm);
	}
}
