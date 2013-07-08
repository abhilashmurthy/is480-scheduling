/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import com.opensymphony.xwork2.ActionSupport;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import manager.MilestoneManager;
import manager.ScheduleManager;
import manager.TermManager;
import model.Milestone;
import model.Schedule;
import model.Term;
import model.Timeslot;

/**
 *
 * @author suresh
 */
public class GetTimeslotsAction extends ActionSupport{

	@Override
	public String execute() throws Exception {
		Term term = TermManager.findByYearAndSemester(2013, 1);
		Milestone milestone = MilestoneManager.findByName("Acceptance");
		Schedule activeSchedule = ScheduleManager.findByTermAndMilestone(term, milestone);
		Set<Timeslot> timeslots = activeSchedule.getTimeslots();
		for (Timeslot t : timeslots) {
			HashMap<String, String> map = new HashMap<String, String>();
			Calendar start = Calendar.getInstance();
			start.setTimeInMillis(t.getStartTime().getTime());
			map.put("date", start.get(Calendar.DATE) + " "
					+ start.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
					+ start.get(Calendar.YEAR));
			map.put("startTime", start.get(Calendar.HOUR) + " "
					+ start.get(Calendar.MINUTE) + " "
					+ start.get(Calendar.AM_PM));
			map.put("endTime", (start.get(Calendar.HOUR) + 1) + " "
					+ start.get(Calendar.MINUTE) + " "
					+ start.get(Calendar.AM_PM));
			if (t.getTeam() != null) {
				map.put("teamName", t.getTeam().getTeamName());
			}
			data.add(map);
		}
		return SUCCESS;
	}
	
	private ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

	public ArrayList<HashMap<String, String>> getData() {
		return data;
	}
	
	public void setData(ArrayList<HashMap<String, String>> data) {
		this.data = data;
	}
	
}
