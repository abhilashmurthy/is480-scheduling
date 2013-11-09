/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.BookingStatus;
import constant.Response;
import constant.Role;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.BookingManager;
import model.Booking;
import model.Milestone;
import model.SystemActivityLog;
import model.Team;
import model.Timeslot;
import model.User;
import model.role.Faculty;
import model.role.Student;
import model.role.TA;
import notification.email.ConfirmedBookingEmail;
import notification.email.NewBookingEmail;
import notification.email.RespondToBookingEmail;
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
	private boolean overrideApproval = false;
    private HashMap<String, Object> json = new HashMap<String, Object>();

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String execute() throws Exception {
		HttpSession session = request.getSession();
		
		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());
		
		SystemActivityLog logItem = new SystemActivityLog();
		logItem.setActivity("Booking: Create");
		logItem.setRunTime(now);
		logItem.setUser((User)session.getAttribute("user"));
		logItem.setMessage("Error with validation / No changes made");
		logItem.setSuccess(true);
		
        EntityManager em = null;
        try {
            json.put("exception", false);
            em = MiscUtil.getEntityManagerInstance();
            Map parameters = request.getParameterMap();

            User user = (User) session.getAttribute("user");
            Role activeRole = (Role) session.getAttribute("activeRole");
            Team team = null;

            //Retrieving the team information
            if (activeRole == Role.STUDENT) {
                Student s = em.find(Student.class, user.getId());
                team = s.getTeam();
            } else if (activeRole == Role.ADMINISTRATOR || activeRole == Role.COURSE_COORDINATOR) {
                try {
                    team = em.find(Team.class, teamId);
                } catch (Exception e) {
                    logger.error("Database Operation Error");
					if (MiscUtil.DEV_MODE) {
						for (StackTraceElement s : e.getStackTrace()) {
							logger.debug(s.toString());
						}
					}
                    throw new Exception("Unable to find team");
                }
            }

            //Retrieving the chosen timeslot
            Timeslot timeslot = null;
            if (timeslotId != null) {
                timeslot = em.find(Timeslot.class, timeslotId);
            }
			em.getTransaction().begin();
			json = BookingManager.createBooking(em, timeslot, user, team, overrideApproval);
			
			//Test SMS
//			BookingManager.testSMS((Long) ((HashMap) json.get("booking")).get("bookingId"), request);
			
			em.getTransaction().commit();
			//TODO Activity logging needs to be handled!
			if (json.containsKey("success")) {
				if ((Boolean)json.get("success") == true) {
					logItem.setMessage("Booking was created successfully: " + timeslot.getCurrentBooking().toString());
				}
			}
			return SUCCESS; 
        } catch (Exception e) {
			logItem.setSuccess(false);
			User userForLog = (User) session.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("exception", true);
            json.put("message", "Error with CreateBooking: Escalate to developers!");
        } finally {
           if (em != null) {
				//Saving job log in database
				if (!em.getTransaction().isActive()) em.getTransaction().begin();
				em.persist(logItem);
				em.getTransaction().commit();
				
				if (em.getTransaction().isActive()) em.getTransaction().rollback();
				if (em.isOpen()) em.close();
			}
        }
        return SUCCESS;
    }

    private boolean validateInformation(EntityManager em, Team team, Timeslot timeslot, User user) {
        // Checking if team information is found
        if (team == null) {
            logger.error("Team information not found or unauthorized user role");
            json.put("success", false);
            json.put("message", "Team unidentified or you may not have the required"
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
		
		if (!timeslot.getSchedule().isBookable()) {
			logger.error("Schedule not open for booking");
            json.put("success", false);
            json.put("message", "This milestone is currently not available for booking");
            return false;
		}
		
		//Check if the timeslot has already passed (Not applicable for Administrator and Course Coordinator)
		if (user.getRole() != Role.ADMINISTRATOR && user.getRole() != Role.COURSE_COORDINATOR) {
			Calendar now = Calendar.getInstance();
			if (timeslot.getStartTime().before(now.getTime())) {
				json.put("success", false);
				json.put("message", "You cannot book a timeslot that has already passed!");
				return false;
			}	
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
            json.put("message", "Team already has an active booking in the current schedule");
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
