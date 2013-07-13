/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.Calendar;
import model.Schedule;
import model.Term;
import model.dao.ScheduleDAO;
import model.dao.TermDAO;

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
		int year, term;
		Calendar now = Calendar.getInstance();
		if (now.get(Calendar.MONTH) <= Calendar.MAY && now.get(Calendar.DATE) <= 15) {
			year = now.get(Calendar.YEAR) - 1;
			term = 2;
		} else if ((now.get(Calendar.MONTH) >= Calendar.MAY && now.get(Calendar.DATE) > 15)
				&& (now.get(Calendar.MONTH) <= Calendar.DECEMBER && now.get(Calendar.DATE) <= 20)) {
			year = now.get(Calendar.YEAR);
			term = 1;
		} else {
			year = now.get(Calendar.YEAR);
			term = 2;
		}
		
		return TermDAO.findByYearAndTerm(2013, 1);
	}
	
	public static Schedule getActiveSchedule() {
		Term activeTerm = getActiveTerm();
		return ScheduleDAO.findByScheduleId(activeTerm.getId(), Milestone.ACCEPTANCE);
	}
}
