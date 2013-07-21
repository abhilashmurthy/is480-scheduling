/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import com.opensymphony.xwork2.ActionSupport;
import java.text.SimpleDateFormat;
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
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		Term term = TermManager.findByYearAndSemester(2013, "Term 1");
		Milestone milestone = MilestoneManager.findByName("Acceptance");
		Schedule activeSchedule = ScheduleManager.findByTermAndMilestone(term, milestone);
		json.put("startDate", dateFormat.format(activeSchedule.getStartDate()));
		json.put("endDate", dateFormat.format(activeSchedule.getEndDate()));
		
		ArrayList<HashMap<String, Object>> mapList = new ArrayList<HashMap<String, Object>>();
		for (Timeslot t : activeSchedule.getTimeslots()) {
			
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("date", dateFormat.format(t.getStartTime()));
			map.put("startTime", timeFormat.format(t.getStartTime()));
			map.put("endTime", timeFormat.format(t.getEndTime()));
			if (t.getTeam() != null) {
				map.put("teamName", t.getTeam().getTeamName());
			}
			mapList.add(map);
		}
		json.put("timeslots", mapList);
		return SUCCESS;
	}
	
	private HashMap<String, Object> json = new HashMap<String, Object>();

	public HashMap<String, Object> getJson() {
		return json;
	}

	public void setJson(HashMap<String, Object> json) {
		this.json = json;
	}
	
}
