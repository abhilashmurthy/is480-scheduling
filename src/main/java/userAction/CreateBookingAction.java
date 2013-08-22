/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Response;
import constant.Role;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.BookingManager;
import model.Booking;
import model.Milestone;
import model.Team;
import model.Timeslot;
import model.User;
import model.role.Student;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class CreateBookingAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(CreateBookingAction.class);
    private Long timeslotId;
	private Long teamId;
    private HashMap<String, Object> json = new HashMap<String, Object>();

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String execute() throws Exception {
        try {
            json.put("exception", false);
            EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
            HttpSession session = request.getSession();

            User user = (User) session.getAttribute("user");
            Role activeRole = (Role) session.getAttribute("activeRole");
            Team team = null;

			//Retrieving the team information
            if (activeRole.equals(Role.STUDENT)) {
				Student s = em.find(Student.class, user.getId());
                team = s.getTeam();
            } else if (activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)) {
                try {
                    team = em.find(Team.class, teamId);
                } catch (Exception e) {
                    logger.error("Database Operation Error");
                    throw new Exception("Unable to find team");
                }
            }
			
			//Retrieving the chosen timeslot
			Timeslot timeslot = null;
			if (timeslotId != null) {
				timeslot = em.find(Timeslot.class, timeslotId);
			}
			
            //Validating information provided by the front end
            if (!validateInformation(em, team, timeslot)) {
                    return SUCCESS;
            }

            try {
                em.getTransaction().begin();
				
				Booking booking = new Booking();
				
				//Assign information to booking
				booking.setTimeslot(timeslot);
                booking.setTeam(team);
				booking.setCreatedAt(new Timestamp(Calendar.getInstance().getTimeInMillis()));

                //Add team members to attendees
                HashSet<User> reqAttendees = new HashSet<User>();
                reqAttendees.addAll(team.getMembers());

                //Create booking response entries based on milestone
				//TODO Remove hardcoding after milestone management is implemented
                HashMap<User, Response> responseList = new HashMap<User, Response>();
				Milestone milestone = timeslot.getSchedule().getMilestone();
                if (milestone.getName().equalsIgnoreCase("acceptance")) {
                    responseList.put(team.getSupervisor(), Response.PENDING);
                    reqAttendees.add(team.getSupervisor());
                } else if (milestone.getName().equalsIgnoreCase("midterm")) {
                    responseList.put(team.getReviewer1(), Response.PENDING);
                    reqAttendees.add(team.getReviewer1());
					responseList.put(team.getReviewer2(), Response.PENDING);
                    reqAttendees.add(team.getReviewer2());
                } else if (milestone.getName().equalsIgnoreCase("final")) {
					responseList.put(team.getSupervisor(), Response.PENDING);
                    reqAttendees.add(team.getSupervisor());
                    responseList.put(team.getReviewer1(), Response.PENDING);
                    reqAttendees.add(team.getReviewer1());
                } else {
                    logger.error("FATAL ERROR: Code not to be reached!");
                    throw new Exception();
                }

                booking.setResponseList(responseList);
                booking.setRequiredAttendees(reqAttendees);
//                NewBookingEmail newEmail = new NewBookingEmail(bookingSlot);
//				RespondToBookingEmail responseEmail = new RespondToBookingEmail(bookingSlot);
//                newEmail.sendEmail();
//				responseEmail.sendEmail();
                em.persist(booking);
				
				//Setting the current active booking in the timeslot object
				timeslot.setCurrentBooking(booking);
				em.persist(timeslot);
				
                em.getTransaction().commit();
            } catch (Exception e) {
                //Rolling back write operations
                em.getTransaction().rollback();
                logger.error("FATAL ERROR: Database Write Error. Code not to be reached!");
                json.put("success", false);
                json.put("message", "Oops. Something went wrong on our end. Please try again!");
                return SUCCESS;
            }

            json.put("success", true);
            json.put("message", "Booking created successfully! Confirmation email has been sent to all attendees.");
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("exception", true);
            json.put("message", "Error with CreateBooking: Escalate to developers!");
        }
        return SUCCESS;
    }
	
	private boolean validateInformation(EntityManager em, Team team, Timeslot timeslot) {
		// Checking if team information is found
		if (team == null) {
			logger.error("Team information not found or unauthorized user role");
			json.put("success", false);
			json.put("message", "Team not identified or you do not have required"
					+ " permissions to make a booking.");
			return false;
		}

		//Check if the timeslot is found
		if (timeslot == null) {
			logger.error("Timeslot not found");
			json.put("success", false);
			json.put("message", "Timeslot not found. Please check the ID provided!");
			return false;
		}

		//Check if the timeslot is free
		if (timeslot.getCurrentBooking() != null) { //Slot is full
			json.put("success", false);
			json.put("message", "Oops. This timeslot is already taken."
					+ " Please book another slot!");
			return false;
		}
		
		//Check if the team has already made a booking for the current schedule
		ArrayList<Booking> activeBookings = BookingManager.getActiveByTeamAndSchedule(em, team, timeslot.getSchedule());
		if (!activeBookings.isEmpty()) {
			json.put("success", false);
			json.put("message", "Team already has a booking in the current schedule");
			return false;	
		}
		
		return true;
	}

    
	public Long getTeamId() {
		return teamId;
	}

	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}

	public Long getTimeslotId() {
		return timeslotId;
	}

	public void setTimeslotId(Long timeslotId) {
		this.timeslotId = timeslotId;
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
