/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Status;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
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
    private String date;
    private String startTime;
    private String endTime;
    private String termId;
    private String milestoneStr;
    private String teamName;

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
    private HashMap<String, Object> json = new HashMap<String, Object>();
	private Milestone milestone = null;
	private Timeslot bookingSlot = null;

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
            String activeRole = (String) session.getAttribute("activeRole");
            Team team = null;

            if (activeRole.equalsIgnoreCase("Student")) {
                team = user.getTeam();
            } else if (activeRole.equalsIgnoreCase("Administrator")) {
                EntityTransaction transaction = em.getTransaction();
                try {
                    transaction.begin();
                    Query q = em.createQuery("Select t from Team t where teamName = :teamName")
                            .setParameter("teamName", teamName);
                    team = (Team) q.getSingleResult();
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("Database Operation Error");
                    throw new Exception("Unable to find team");
                }
            }
			
            //Validating information provided by the front end
            if (!validateInformation(em, team)) {
                    return SUCCESS;
            }

            try {
                em.getTransaction().begin();

                //Assign timeslot to team
                bookingSlot.setTeam(team);

                //Add team members to attendees
                HashSet<User> attendees = new HashSet<User>();
                attendees.addAll(team.getMembers());

                //Create timeslot status entries based on milestone
                HashMap<User, Status> statusList = new HashMap<User, Status>();
                if (milestone.getName().equalsIgnoreCase("acceptance")) {
                    statusList.put(team.getSupervisor(), Status.PENDING);
                    attendees.add(team.getSupervisor());
                } else if (milestone.getName().equalsIgnoreCase("midterm")) {
                    statusList.put(team.getReviewer1(), Status.PENDING);
                    attendees.add(team.getReviewer1());
                    statusList.put(team.getReviewer2(), Status.PENDING);
                    attendees.add(team.getReviewer2());
                } else if (milestone.getName().equalsIgnoreCase("final")) {
                    statusList.put(team.getSupervisor(), Status.PENDING);
                    attendees.add(team.getSupervisor());
                    statusList.put(team.getReviewer1(), Status.PENDING);
                    attendees.add(team.getReviewer1());
                } else {
                    logger.error("FATAL ERROR: Code not to be reached!");
                    throw new Exception();
                }

                bookingSlot.setStatusList(statusList);
                bookingSlot.setAttendees(attendees);
                NewBookingEmail newEmail = new NewBookingEmail(bookingSlot);
				RespondToBookingEmail responseEmail = new RespondToBookingEmail(bookingSlot);
                newEmail.sendEmail();
				responseEmail.sendEmail();
                em.persist(bookingSlot);
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
	
	private boolean validateInformation(EntityManager em, Team team) {
		// Checking if team information is found
		if (team == null) {
			logger.error("Team information not found or unauthorized user role");
			json.put("success", false);
			json.put("message", "Team not identified or you do not have required"
					+ " permissions to make a booking.");
			return false;
		}

		//Validating milestone info
		milestone = MilestoneManager.findByName(em, milestoneStr);
		if (milestone == null) {
			logger.error("Milestone not found");
			json.put("success", false);
			json.put("message", "Oops. Something went wrong on our end. Please try again!");
			return false;
		}

		//Retreiving the term
		Term term;
		try {
			int academicYear = Integer.valueOf(termId.split(",")[0]);
			String semester = termId.split(",")[1];
			term = TermManager.findByYearAndSemester(em, academicYear, semester);
			if (term == null) {
				throw new Exception();
			}
		} catch (Exception e) {
			logger.error("Term not found");
			logger.error(e.getMessage());
			json.put("success", false);
			json.put("message", "Oops. Something went wrong on our end. Please try again!");
			return false;
		}


		//Retrieve the corresponding schedule object and its timeslots
		Schedule schedule = ScheduleManager.findByTermAndMilestone(em, term, milestone);
		if (schedule == null || schedule.getTimeslots() == null) {
			logger.error("Schedule not found");
			json.put("success", false);
			json.put("message", "Oops. Something went wrong on our end. Please try again!");
			return false;
		}
		Set<Timeslot> timeslots = schedule.getTimeslots();

		//Checking if the team already has a booking (pending/confirmed)
		for (Timeslot t : timeslots) {
			if (t.getTeam() != null && t.getTeam().equals(team)) {
				logger.error("Team's already booked a timeslot for the milestone this term");
				json.put("success", false);
				json.put("message", "Seems like you already have a booking for this milestone."
						+ " Can't let you make a booking!");
				return false;
			}
		}

		//Retrieve the corresponding booking slot
		Timestamp bookingTime;
		try {
			String timestampStr = date + " " + startTime;
			bookingTime = Timestamp.valueOf(timestampStr);
		} catch (IllegalArgumentException e) {
			logger.error("Start time could not be parsed");
			json.put("success", false);
			json.put("message", "Date information not entered correctly. Please try again!");
			return false;
		}

		for (Timeslot t : timeslots) {
			Timestamp tStartTime = t.getStartTime();
			if (tStartTime.equals(bookingTime)) {
				bookingSlot = t;
				break;
			}
		}

		//Check if timeslot has been found
		if (bookingSlot == null) {
			logger.error("Chosen timeslot not found");
			json.put("success", false);
			json.put("message", "We can't find the timeslot you're trying to book."
					+ " Please check the details entered!");
			return false;
		}

		//Check if the timeslot is free
		if (bookingSlot.getTeam() != null) { //Slot is full
			logger.error("Chosen timeslot already booked");
			json.put("success", false);
			json.put("message", "Oops. This timeslot is already taken."
					+ " Please book another slot!");
			return false;
		}
		
		return true;
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
