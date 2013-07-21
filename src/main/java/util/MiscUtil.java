/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.Calendar;
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
	 * Get the current/next active term based on today's date
	 * Cutoff dates:
	 * 15-May and 20-Dec
	 */
	public static Term getActiveTerm() {
		return TermManager.findByYearAndSemester(2013, "Term 1");
	}
	
	public static Schedule getActiveSchedule() {
		Term activeTerm = getActiveTerm();
		return ScheduleManager.findActiveByTerm(activeTerm);
	}
}
