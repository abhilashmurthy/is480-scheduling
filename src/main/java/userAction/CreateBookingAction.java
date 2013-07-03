/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.opensymphony.xwork2.ActionSupport;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.Schedule;
import model.Team;
import model.Timeslot;
import model.TimeslotStatus;
import model.TimeslotStatusPk;
import model.User;
import model.dao.ScheduleDAO;
import model.dao.TeamDAO;
import model.dao.TimeslotDAO;
import model.dao.TimeslotStatusDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Milestone;
import util.Status;
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
		Milestone enumMilestone = (milestone.equalsIgnoreCase("acceptance"))
				? Milestone.ACCEPTANCE
				: (milestone.equalsIgnoreCase("midterm"))
				? Milestone.MIDTERM
				: Milestone.FINAL ;
		Schedule schedule = ScheduleDAO.findByScheduleId(termId, enumMilestone);
		List<Timeslot> timeslots = schedule.getTimeslots();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp bookingTime = Timestamp.valueOf(startTime);
        
		//Checking if the team already has a booking (pending/confirmed)
		for (Timeslot t : timeslots) {
			if (t.getTeamId() != null && t.getTeamId().intValue() == teamId) {
				response = "This team already has a booking.";
				System.err.println("ERROR: " + response);
				return "fail";
			}
		}
		
		//Retrieve the corresponding booking slot
		Timeslot bookingSlot = null;
		for (Timeslot t : timeslots) {
			Timestamp tStartTime = t.getId().getStartTime();
			logger.debug(sdf.format(tStartTime));
			logger.debug(sdf.format(bookingTime));
			if (tStartTime.equals(bookingTime)) {
				bookingSlot = t;
				break;
			}
		}
		
//		//Check if timeslot has been found
//		if (bookingSlot == null) {
//			logger.error("Invalid details. Timeslot not found.");
//			return ERROR;
//		}
//		
		//Check if the timeslot is free
		if (bookingSlot.getTeamId() != null) { //Slot is full
				response = "This timeslot is already taken.";
				return "fail";
		}
		
		//All conditions met. Assign timeslot to team
		BigInteger bigIntTeamId = BigInteger.valueOf(teamId);
		bookingSlot.setTeamId(bigIntTeamId);
		TimeslotDAO.save(bookingSlot);
		
		//Create timeslot status entries based on milestone
		Milestone bookingMilestone = bookingSlot.getId().getMilestone();
		Team team = TeamDAO.findByTeamId(teamId);
		List<User> confirmationUsers = new ArrayList<User>();
		if (bookingMilestone.equals(Milestone.ACCEPTANCE)) {
			confirmationUsers.add(team.getSupervisor());
		} else if (bookingMilestone.equals(Milestone.MIDTERM)) {
			confirmationUsers.add(team.getReviewer1());
			confirmationUsers.add(team.getReviewer2());
		} else if (bookingMilestone.equals(Milestone.FINAL)) {
			confirmationUsers.add(team.getSupervisor());
			confirmationUsers.add(team.getReviewer1());
		} else {
			//TODO Pending
		}
		
		for (User u : confirmationUsers) {
			TimeslotStatusPk statusPk = new TimeslotStatusPk(
					bookingSlot.getId().getTermId(),
					bookingSlot.getId().getMilestone(),
					bookingSlot.getId().getStartTime(),
					u.getId());
			TimeslotStatus status = new TimeslotStatus();
			status.setId(statusPk);
			status.setStatus(Status.PENDING);
			TimeslotStatusDAO.save(status);
		}
		
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
