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
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import manager.MilestoneManager;
import manager.ScheduleManager;
import manager.TermManager;
import model.Milestone;
import model.Schedule;
import model.Term;
import model.Timeslot;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class GetScheduleAction extends ActionSupport{

	@Override
	public String execute() throws Exception {
		EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
		
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		Term term = TermManager.findByYearAndSemester(em, 2013, "Term 1");
		Milestone milestone = MilestoneManager.findByName(em, "Acceptance");
		Schedule activeSchedule = ScheduleManager.findByTermAndMilestone(em, term, milestone);
		json.put("startDate", dateFormat.format(activeSchedule.getStartDate()));
		json.put("endDate", dateFormat.format(activeSchedule.getEndDate()));
		
		ArrayList<HashMap<String, Object>> mapList = new ArrayList<HashMap<String, Object>>();
		for (Timeslot t : activeSchedule.getTimeslots()) {
			
			HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put("id", t.getId());
			map.put("datetime", dateFormat.format(t.getStartTime()) + " " + timeFormat.format(t.getStartTime()));
//			map.put("startTime", timeFormat.format(t.getStartTime()));
//			map.put("endTime", timeFormat.format(t.getEndTime()));
			if (t.getTeam() != null) {
				map.put("team", t.getTeam().getTeamName());
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
