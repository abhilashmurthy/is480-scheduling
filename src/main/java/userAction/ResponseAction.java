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
	private int teamId;
	private String milestone;
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
		Term term = MiscUtil.getActiveTerm();
		termId = term.getId();
		
//		//Checking whether the user is a reviewer/supervisor or not
//		//Getting TimeSlotStatus based on term id and user id
		List<TimeslotStatus> ts = TimeslotStatusDAO.findTimeSlotStatusByTermAndUser(termId, userId);
		if (ts.size() == 0) {
			//request.setAttribute("error", "You cannot Approve/Reject a booking!");
            logger.error("User cannot access Approve/Reject Booking");
            return ERROR;
		}
		
		//Checking the timeslots for which the status is pending
        for(TimeslotStatus t: ts) {
            Timeslot timeslotDetails = TimeslotDAO.findByDate(t.getId().getStartTime());
			HashMap<String, String> map = new HashMap<String, String>();
			
			//Retrieving time slot details and displaying it
            if(t.getStatus().toString().equals("PENDING")){
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
                teamId = timeslotDetails.getTeamId().intValue();				
				teamName = TeamDAO.findByTeamId(teamId).getTeamName();
				milestone = timeslotDetails.getId().getMilestone().toString();
				startTime = sdf.format(timeslotDetails.getId().getStartTime());
				endTime = sdf.format(timeslotDetails.getEndTime());
				
				map.put("teamId", String.valueOf(teamId));
				map.put("teamName", teamName);
				map.put("milestone", milestone);
				map.put("startTime", startTime);
				map.put("endTime", endTime);
            }
			data.add(map);
        }
        return SUCCESS;
    }

	//Getters and Setters
	public int getTeamId() {
		  return teamId;
	}

	public void setTeamIdInt(int teamId) {
		  this.teamId = teamId;
	}

	public long getUserId(){
		return userId;
	}

	public long getTermId(){
		return termId;
	}

	public void setUserId(long userId){
		this.userId = userId;
	}

	public void setTermId(long termId){
		this.termId = termId;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public String getMilestone() {
		return milestone;
	}

	public void setMilestone(String milestone) {
		this.milestone = milestone;
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

	public void setServletRequest(HttpServletRequest hsr) {
		this.request = hsr;
	}
}
