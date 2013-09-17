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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import model.Team;
import model.Timeslot;
import model.User;
import model.role.Student;
import model.role.TA;
import notification.email.NewBookingEmail;
import notification.email.RespondToBookingEmail;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systemAction.quartz.SMSReminderJob;
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
        EntityManager em = null;
        try {
            json.put("exception", false);
            em = MiscUtil.getEntityManagerInstance();
            HttpSession session = request.getSession();
            Map parameters = request.getParameterMap();
            String[] optionalAttendeesArray = (String[]) parameters.get("attendees[]");
            if (optionalAttendeesArray != null) logger.debug("Optional 1: " + optionalAttendeesArray[0]);

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
            
            //JSON Return for create booking
            HashMap<String, Object> map = new HashMap<String, Object>();
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat viewDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
            SimpleDateFormat viewTimeFormat = new SimpleDateFormat("HH:mm");
            
            try {
                em.getTransaction().begin();

                Booking booking = new Booking();

                //Assign information to booking
                booking.setTimeslot(timeslot); 
                booking.setTeam(team);
				Timestamp currentTime = new Timestamp(Calendar.getInstance().getTimeInMillis());
                booking.setCreatedAt(currentTime);
                
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
                
                //Add optional attendees
                HashSet<String> optionalAttendees = new HashSet<String>();
                if (optionalAttendeesArray != null) optionalAttendees = new HashSet<String>(Arrays.asList(optionalAttendeesArray));

                booking.setResponseList(responseList);
                booking.setRequiredAttendees(reqAttendees);
                booking.setOptionalAttendees(optionalAttendees);
                booking.setLastEditedBy(user.getFullName());
                booking.setLastEditedAt(new Timestamp(Calendar.getInstance().getTimeInMillis()));
                NewBookingEmail newEmail = new NewBookingEmail(booking);
                RespondToBookingEmail responseEmail = new RespondToBookingEmail(booking);
                newEmail.sendEmail();
                responseEmail.sendEmail();
                em.persist(booking);

                //Setting the current active booking in the timeslot object
                timeslot.setCurrentBooking(booking);
                em.persist(timeslot);
                
                map.put("id", timeslot.getId());
                map.put("datetime", dateFormat.format(timeslot.getStartTime()) + " " + timeFormat.format(timeslot.getStartTime()));
                map.put("time", viewTimeFormat.format(timeslot.getStartTime()) + " - " + viewTimeFormat.format(timeslot.getEndTime()));
                map.put("venue", timeslot.getVenue());
                map.put("team", team.getTeamName());
                map.put("startDate", viewDateFormat.format(new Date(timeslot.getStartTime().getTime())));
                map.put("status", booking.getBookingStatus().toString());
                
                //Adding all students
                List<HashMap<String, String>> students = new ArrayList<HashMap<String, String>>();
                Set<Student> teamMembers = team.getMembers();
                for (User studentUser : teamMembers) {
                    HashMap<String, String> studentMap = new HashMap<String, String>();
                    studentMap.put("name", studentUser.getFullName());
                    students.add(studentMap);
                }
                map.put("students", students);
                
                //Adding all faculty and their status
                List<HashMap<String, String>> faculties = new ArrayList<HashMap<String, String>>();
                HashMap<User, Response> statusList = responseList;
                for (User facultyUser : statusList.keySet()) {
                    HashMap<String, String> facultyMap = new HashMap<String, String>();
                    facultyMap.put("name", facultyUser.getFullName());
                    facultyMap.put("status", statusList.get(facultyUser).toString());
                    faculties.add(facultyMap);
                }
                 map.put("faculties", faculties);
                
                //Adding all optionals
                List<HashMap<String, String>> optionals = new ArrayList<HashMap<String, String>>();
                for (String optional : optionalAttendees) {
                    HashMap<String, String> optionalMap = new HashMap<String, String>();
                    optionalMap.put("id", optional);
                    optionalMap.put("name", optional);
                    optionals.add(optionalMap);
                }
                map.put("optionals", optionals);
               
				TA ta = timeslot.getTA();
                String TA = (ta != null) ? ta.getFullName() : "-";
                map.put("TA", TA);
                String teamWiki = "-";
                map.put("teamWiki", teamWiki);
                
                json.put("booking", map);

                em.getTransaction().commit();
				
				//Schedule job for SMS reminders
				scheduleSMSReminder(booking);
            } catch (Exception e) {
                //Rolling back write operations
                logger.error("Exception caught: " + e.getMessage());
                if (MiscUtil.DEV_MODE) {
                    for (StackTraceElement s : e.getStackTrace()) {
                        logger.debug(s.toString());
                    }
                }
                em.getTransaction().rollback();
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
        } finally {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return SUCCESS;
    }

    private boolean validateInformation(EntityManager em, Team team, Timeslot timeslot) {
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
		
		//Check if the timeslot has already passed
		Calendar now = Calendar.getInstance();
		if (timeslot.getStartTime().before(now.getTime())) {
			json.put("success", false);
            json.put("message", "You cannot book a timeslot that has already passed!");
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
            json.put("message", "Team already has an active booking in the current schedule");
            return false;
        }

        return true;
    }

	//Method to schedule a job to send an SMS reminder 24 hrs before the presentation
	private void scheduleSMSReminder(Booking b) throws Exception {
		StdSchedulerFactory factory = (StdSchedulerFactory) request.getSession()
				.getServletContext()
				.getAttribute(QuartzInitializerListener.QUARTZ_FACTORY_KEY);
		Scheduler scheduler = factory.getScheduler();
		
		JobDetail jd = JobBuilder.newJob(SMSReminderJob.class)
				.usingJobData("bookingId", b.getId())
				.withIdentity(b.getId().toString(),"SMS Reminders").build();
		
		//Calculating the time to trigger the job
		Calendar scheduledTime = Calendar.getInstance();
		Timestamp presentationStartTime = b.getTimeslot().getStartTime();
		scheduledTime.setTime(presentationStartTime);
		scheduledTime.add(Calendar.DAY_OF_MONTH, -1); //Subtracting a day from the presentation start time
//		scheduledTime.add(Calendar.SECOND, 10); //For testing
		
		Trigger tr = TriggerBuilder.newTrigger().withIdentity(b.getId().toString(),"SMS Reminders")
				.startAt(scheduledTime.getTime()).build();
		
		scheduler.scheduleJob(jd, tr);
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
