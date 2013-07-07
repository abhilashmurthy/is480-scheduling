/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import com.opensymphony.xwork2.ActionSupport;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import model.Schedule;
import model.Team;
import model.Timeslot;
import model.dao.TeamDAO;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class GetTimeslotsAction extends ActionSupport{

	@Override
	public String execute() throws Exception {
		Schedule activeSchedule = MiscUtil.getActiveSchedule();
		List<Timeslot> timeslots = activeSchedule.getTimeslots();
		for (Timeslot t : timeslots) {
			HashMap<String, String> map = new HashMap<String, String>();
			Calendar start = Calendar.getInstance();
			start.setTimeInMillis(t.getId().getStartTime().getTime());
			map.put("date", start.get(Calendar.DATE) + " "
					+ start.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
					+ start.get(Calendar.YEAR));
			map.put("startTime", start.get(Calendar.HOUR) + " "
					+ start.get(Calendar.MINUTE) + " "
					+ start.get(Calendar.AM_PM));
			map.put("endTime", (start.get(Calendar.HOUR) + 1) + " "
					+ start.get(Calendar.MINUTE) + " "
					+ start.get(Calendar.AM_PM));
			if (t.getTeamId() != null) {
				Team team = TeamDAO.findByTeamId(t.getTeamId().intValue());
				map.put("teamName", team.getTeamName());
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
