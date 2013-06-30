/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.opensymphony.xwork2.ActionSupport;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import model.Schedule;
import model.Timeslot;
import model.dao.ScheduleDAO;
import model.dao.TimeslotDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Prakhar
 */
public class CreateBookingAction extends ActionSupport{
    
    private int teamId;
    private String startTime;
	private String endTime;
    private int termId;
    private String milestone;
	private String response;

	static final Logger logger = LoggerFactory.getLogger(CreateBookingAction.class);
	
    @Override
    public String execute() throws Exception {
		//Retrieve the corresponding schedule object and its timeslots
		Schedule schedule = ScheduleDAO.findByScheduleId(termId, milestone);
		List<Timeslot> timeslots = schedule.getTimeslots();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date bookingTime = sdf.parse(startTime);
        
		//Checking if the team already has a booking (pending/confirmed)
		for (Timeslot t : timeslots) {
			if (t.getTeamId().intValue() == teamId) {
				response = "This team already has a booking.";
				return "fail";
			}
		}
		
		//Retrieve the corresponding booking slot
		Timeslot bookingSlot = null;
		for (Timeslot t : timeslots) {
			Date tStartTime = t.getId().getStartTime();
			if (tStartTime.equals(bookingTime)) {
				bookingSlot = t;
				break;
			}
		}
		
		//Check if timeslot has been found
		if (bookingSlot == null) {
			logger.error("Invalid details. Timeslot not found.");
			return ERROR;
		}
		
		//Check if the timeslot is free
		if (bookingSlot.getTeamId() != null) { //Slot is full
				response = "This timeslot is already taken.";
				return "fail";
		}
		
		//All conditions met. Assign timeslot to team
		BigInteger bigIntTeamId = BigInteger.valueOf(teamId);
		bookingSlot.setTeamId(bigIntTeamId);
		TimeslotDAO.save(bookingSlot);
		
		return SUCCESS;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
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

    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
    }

    public String getMilestone() {
        return milestone;
    }

    public void setMilestone(String milestone) {
        this.milestone = milestone;
    }
	
	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
    
}
