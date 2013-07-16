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
		int year, semester;
		Calendar now = Calendar.getInstance();
		// Testing code if today's date needs to be changed for testing
//		now.set(Calendar.DAY_OF_MONTH, 1);
//		now.set(Calendar.MONTH, 1);
//		now.set(Calendar.YEAR, 2013);
		
		if (now.get(Calendar.MONTH) <= Calendar.MAY && now.get(Calendar.DATE) <= 15) {
			year = now.get(Calendar.YEAR) - 1;
			semester = 2;
		} else if (now.get(Calendar.MONTH) <= Calendar.DECEMBER && now.get(Calendar.DATE) <= 20) {
			year = now.get(Calendar.YEAR);
			semester = 1;
		} else {
			year = now.get(Calendar.YEAR);
			semester = 2;
		}
		
		return TermManager.findByYearAndSemester(year, semester);
	}
	
	public static Schedule getActiveSchedule() {
		Term activeTerm = getActiveTerm();
		return ScheduleManager.findActiveByTerm(activeTerm);
	}
}
