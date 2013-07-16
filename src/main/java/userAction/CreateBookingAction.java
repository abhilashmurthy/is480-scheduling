/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.google.gson.Gson;
import com.opensymphony.xwork2.ActionSupport;
import constant.Status;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.MilestoneManager;
import manager.ScheduleManager;
import manager.TermManager;
import model.Milestone;
import model.Schedule;
import model.Team;
import model.Term;
import model.Timeslot;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Prakhar
 */
public class CreateBookingAction extends ActionSupport implements ServletRequestAware {

	private String date;
	private String startTime;
	private String endTime;
	private String termId;
	private String milestoneStr;
	private HttpServletRequest request;
	private HashMap<String, Object> json = new HashMap<String, Object>();

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
	static final Logger logger = LoggerFactory.getLogger(CreateBookingAction.class);

	@Override
	public String execute() throws Exception {
		HttpSession session = request.getSession();

		User user = (User) session.getAttribute("user");
		Team team = user.getTeam();

		// Checking if the user is part of any team
		if (team == null) {
			request.setAttribute("error", "Doesn't look like you're part of any team."
					+ " Can't let you make a booking!");
			logger.error("User's team information not found");
			json.put("success", false);
			json.put("message", "Doesn't look like you're part of any team."
					+ " Can't let you make a booking!");
			return SUCCESS;
		}

		//Validating milestone info
		Milestone milestone = MilestoneManager.findByName(milestoneStr);
		if (milestone == null) {
			request.setAttribute("error", "Oops. Something went wrong on our end. Please try again!");
			logger.error("Milestone not found");
			json.put("success", false);
			json.put("message", "Oops. Something went wrong on our end. Please try again!");
			return SUCCESS;
		}

		//Retreiving the term
		Term term;
		try {
			int academicYear = Integer.valueOf(termId.split(",")[0]);
			int semester = Integer.valueOf(termId.split(",")[1]);
			term = TermManager.findByYearAndSemester(academicYear, semester);
			if (term == null) {
				throw new Exception();
			}
		} catch (Exception e) {
			request.setAttribute("error", "Oops. Something went wrong on our end. Please try again!");
			logger.error("Term not found");
			json.put("success", false);
			json.put("message", "Oops. Something went wrong on our end. Please try again!");
			return SUCCESS;
		}


		//Retrieve the corresponding schedule object and its timeslots
		Schedule schedule = ScheduleManager.findByTermAndMilestone(term, milestone);
		if (schedule == null || schedule.getTimeslots() == null) {
			request.setAttribute("error", "Oops. Something went wrong on our end. Please try again!");
			logger.error("Schedule not found");
			json.put("success", false);
			json.put("message", "Oops. Something went wrong on our end. Please try again!");
			return SUCCESS;
		}
		Set<Timeslot> timeslots = schedule.getTimeslots();

		//Checking if the team already has a booking (pending/confirmed)
		for (Timeslot t : timeslots) {
			if (t.getTeam() != null && t.getTeam().equals(team)) {
				request.setAttribute("error", "Seems like you already have a booking for this milestone."
						+ " Can't let you make a booking!");
				logger.error("Team's already booked a timeslot for the milestone this term");
				json.put("success", false);
				json.put("message", "Seems like you already have a booking for this milestone."
						+ " Can't let you make a booking!");
				return SUCCESS;
			}
		}

		//Retrieve the corresponding booking slot
		Timestamp bookingTime;
		try {
			String timestampStr = date + " " + startTime;
			bookingTime = Timestamp.valueOf(timestampStr);
		} catch (IllegalArgumentException e) {
			request.setAttribute("error", "Date information not entered correctly. Please try again!");
			logger.error("Start time could not be parsed");
			json.put("success", false);
			json.put("message", "Date information not entered correctly. Please try again!");
			return SUCCESS;
		}
		Timeslot bookingSlot = null;
		for (Timeslot t : timeslots) {
			Timestamp tStartTime = t.getStartTime();
			if (tStartTime.equals(bookingTime)) {
				bookingSlot = t;
				break;
			}
		}

		//Check if timeslot has been found
		if (bookingSlot == null) {
			request.setAttribute("error", "We can't find the timeslot you're trying to book."
					+ " Please check the details entered!");
			logger.error("Chosen timeslot not found");
			json.put("success", false);
			json.put("message", "We can't find the timeslot you're trying to book."
					+ " Please check the details entered!");
			return SUCCESS;
		}

		//Check if the timeslot is free
		if (bookingSlot.getTeam() != null) { //Slot is full
			request.setAttribute("error", "Oops. This timeslot is already taken."
					+ " Please book another slot!");
			logger.error("Chosen timeslot already booked");
			json.put("success", false);
			json.put("message", "Oops. This timeslot is already taken."
					+ " Please book another slot!");
			return SUCCESS;
		}

		//All conditions met. Begin persistence transactions
		EntityManager em = Persistence.createEntityManagerFactory("scheduler").createEntityManager();
		try {
			em.getTransaction().begin();

			//Assign timeslot to team
			bookingSlot.setTeam(team);

			//Create timeslot status entries based on milestone
			HashMap<User, Status> statusList = new HashMap<User, Status>();
			if (milestone.getName().equalsIgnoreCase("acceptance")) {
				statusList.put(team.getSupervisor(), Status.PENDING);
			} else if (milestone.getName().equalsIgnoreCase("midterm")) {
				statusList.put(team.getReviewer1(), Status.PENDING);
				statusList.put(team.getReviewer2(), Status.PENDING);
			} else if (milestone.getName().equalsIgnoreCase("final")) {
				statusList.put(team.getSupervisor(), Status.PENDING);
				statusList.put(team.getReviewer1(), Status.PENDING);
			} else {
				request.setAttribute("error", "Oops. Something went wrong on our end. Please try again!");
				logger.error("FATAL ERROR: Code not to be reached!");
				throw new Exception();
			}

			bookingSlot.setStatusList(statusList);
			em.merge(bookingSlot);
			em.getTransaction().commit();
		} catch (Exception e) {
			//Rolling back database transactions
			em.getTransaction().rollback();
			request.setAttribute("error", "Oops. Something went wrong on our end. Please try again!");
			logger.error("FATAL ERROR: Database Write Error. Code not to be reached!");
			json.put("success", false);
			json.put("message", "Oops. Something went wrong on our end. Please try again!");
			return SUCCESS;
		}

		json.put("success", true);
		json.put("message", "Booking created successfully! Confirmation email has been sent to all attendees. (Coming soon..)");
		
		return SUCCESS;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
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

	public String getTermId() {
		return termId;
	}

	public void setTermId(String termId) {
		this.termId = termId;
	}

	public String getMilestoneStr() {
		return milestoneStr;
	}

	public void setMilestoneStr(String milestoneStr) {
		this.milestoneStr = milestoneStr;
	}
	
	public HashMap<String, Object> getJson() {
		return json;
	}

	public void setJson(HashMap<String, Object> json) {
		this.json = json;
	}
	
	public void setServletRequest(HttpServletRequest hsr) {
		request = hsr;
	}
}
