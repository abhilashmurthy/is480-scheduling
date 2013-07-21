/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.ERROR;
import com.opensymphony.xwork2.ActionSupport;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import model.Schedule;
import model.Team;
import model.Term;
import model.Timeslot;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import constant.Status;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import manager.MilestoneManager;
import manager.ScheduleManager;
import manager.TermManager;
import model.Milestone;
import static userAction.CreateBookingAction.logger;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */

public class ResponseAction extends ActionSupport implements ServletRequestAware{
    private long termId;
    private long userId;
	private String teamName;
	private long teamId;
	private long timeslotId;
	private String milestoneName;
	private String startTime;
	private String endTime;
	private ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
	private HttpServletRequest request;    
    static final Logger logger = LoggerFactory.getLogger(ResponseAction.class);
    
    @Override
    public String execute() throws Exception {
		HttpSession session = request.getSession();
		//Getting the id of user
		User user = (User) session.getAttribute("user");
		userId = user.getId();
		
		//Getting the current term 
//		Term term = MiscUtil.getActiveTerm();
//		termId = term.getId();
		
		//Getting the current schedule based on term id
		Schedule schedule = MiscUtil.getActiveSchedule();
		//Getting the current milestone
		Set<Timeslot> pendingList = null;
		if (schedule != null) {
			Set<Timeslot> allTimeslots = schedule.getTimeslots();
			//Getting the pending timeslots for the particular user
			if (allTimeslots != null && allTimeslots.size() > 0) {
				//Iterating over the timeslots. Matching the user and checking if the status is pending.
				for(Timeslot currentTimeslot: allTimeslots) {
					HashMap<User,Status> statusList = currentTimeslot.getStatusList();
					Iterator iter = statusList.keySet().iterator();
					while (iter.hasNext()) {
						User userObj = (User) iter.next();
						if (userObj == user) {
							//Checking if the status is pending
							if(statusList.get(userObj) == Status.PENDING) {
								pendingList.add(currentTimeslot);
							}
						}
					}
				}
			}
		}
		
		//Putting the pending timeslot details in hash map to display it to the user
		HashMap<String, String> map = new HashMap<String, String>();
		if (pendingList != null && pendingList.size() > 0) {
			for (Timeslot timeslot: pendingList) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
				timeslotId = timeslot.getId();
				teamId = timeslot.getTeam().getId();				
				teamName = timeslot.getTeam().getTeamName();
				milestoneName = schedule.getMilestone().getName();
				startTime = sdf.format(timeslot.getStartTime());
				endTime = sdf.format(timeslot.getEndTime());

				map.put("timeslotId", String.valueOf(timeslotId));
				map.put("teamName", teamName);
				map.put("milestone", milestoneName);
				map.put("startTime", startTime);
				map.put("endTime", endTime);

				data.add(map);
			}
		}
        return SUCCESS;
    }

	public long getTermId() {
		return termId;
	}

	public void setTermId(long termId) {
		this.termId = termId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public long getTeamId() {
		return teamId;
	}

	public void setTeamId(long teamId) {
		this.teamId = teamId;
	}

	public String getMilestoneName() {
		return milestoneName;
	}

	public void setMilestoneName(String milestoneName) {
		this.milestoneName = milestoneName;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public ArrayList<HashMap<String, String>> getData() {
		return data;
	}

	public void setData(ArrayList<HashMap<String, String>> data) {
		this.data = data;
	}

	public long getTimeslotId() {
		return timeslotId;
	}

	public void setTimeslotId(long timeslotId) {
		this.timeslotId = timeslotId;
	}
	
	public void setServletRequest(HttpServletRequest hsr) {
		this.request = hsr;
	}
}