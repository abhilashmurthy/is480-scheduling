/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import constant.BookingStatus;
import constant.Response;
import constant.Role;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import model.Booking;
import model.Milestone;
import model.Schedule;
import model.Settings;
import model.Team;
import model.Term;
import model.Timeslot;
import model.User;
import model.role.Faculty;
import model.role.Student;
import model.role.TA;
import notification.email.ConfirmedBookingEmail;
import notification.email.DeletedBookingEmail;
import notification.email.NewBookingEmail;
import notification.email.RespondToBookingEmail;
import org.hibernate.Hibernate;
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
 * @author suresh
 */
public class BookingManager {
	private static Logger logger = LoggerFactory.getLogger(BookingManager.class);
	
	/**
	 * Get approved/pending bookings by a team in a schedule. Returned list should not be greater than 1.
	 * @param em
	 * @param team
	 * @param schedule
	 * @return 
	 */
	public static ArrayList<Booking> getActiveByTeamAndSchedule(EntityManager em, Team team, Schedule schedule) {
		logger.trace("Getting bookings by team and schedule");
		ArrayList<Booking> list;
		try {
			Query q = em.createQuery("select b from Booking b where b.team = :team"
					+ " and b.timeslot.schedule = :schedule and b.bookingStatus in (:bookingStatus)");
			q.setParameter("team", team);
			q.setParameter("schedule", schedule);
			q.setParameter("bookingStatus", Arrays.asList(BookingStatus.PENDING, BookingStatus.APPROVED));
			list = (ArrayList<Booking>) q.getResultList();
		} catch (Exception e) {
			logger.error("Error in getActiveByTeamAndSchedule()");
			logger.error(e.getMessage());
			return null;
		}
		
		return list;
	}
	
	public static boolean updateBookings(EntityManager em, List<Booking> bookingsToUpdate, EntityTransaction transaction) {
        logger.trace("Updating bookings");
        try {
            transaction.begin();
            for (Booking booking : bookingsToUpdate) {
                em.persist(booking);
            }
            transaction.commit();
            return true;
        } catch (PersistenceException ex) {
            //Rolling back data transactions
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error making database call for update bookings");
            ex.printStackTrace();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return false;
    }
	
	/* To get all bookings for active term */
	public static ArrayList<Booking> getBookingsByTerm (EntityManager em, Term term) {
		logger.trace("Getting all bookings by active term");
		ArrayList<Booking> list = null;
		try {
			Query q = em.createQuery("select b from Booking b where b.timeslot.schedule.milestone.term = :term");
			q.setParameter("term", term);
			list = (ArrayList<Booking>) q.getResultList();
		} catch (Exception e) {
			logger.error("Error in getBookingsByTerm()");
			logger.error(e.getMessage());
			return null;
		}
		return list;
	}
	
	public static ArrayList<Booking> getBookingsByTeam (EntityManager em, Team team) {
		logger.trace("Getting all bookings by team");
		ArrayList<Booking> list;
		try {
			Query q = em.createQuery("select b from Booking b where b.team = :team");
			q.setParameter("team", team);
			list = (ArrayList<Booking>) q.getResultList();
		} catch (Exception e) {
			logger.error("Error in getBookingsByTeam()");
			logger.error(e.getMessage());
			return null;
		}
		return list;
	}
	
	
	
	public static boolean validateBookingInformation
			(EntityManager em, HashMap<String, Object> json,
			Team team, Timeslot timeslot, User user)
	{
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
	
	public synchronized static HashMap<String, Object> createBooking
			(EntityManager em, Timeslot timeslot, User user,
			Team team, boolean overrideApproval)
	{
		HashMap<String, Object> json = new HashMap<String, Object>();
		try {
			//Validating information provided by the front end
			if (!validateBookingInformation(em, json, team, timeslot, user)) {
				return json;
			}

			//JSON Return for create booking
			HashMap<String, Object> map = new HashMap<String, Object>();
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat viewDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
			SimpleDateFormat viewTimeFormat = new SimpleDateFormat("HH:mm");

			Booking booking = new Booking();

			//Assign information to booking
			booking.setTimeslot(timeslot); 
			booking.setTeam(team);
			Timestamp currentTime = new Timestamp(Calendar.getInstance().getTimeInMillis());
			booking.setCreatedAt(currentTime);

			if (overrideApproval) {
				booking.setBookingStatus(BookingStatus.APPROVED);
			}

			//Add team members to attendees
			HashSet<User> reqAttendees = new HashSet<User>();
			reqAttendees.addAll(team.getMembers());

			//Create booking response entries based on required attendees for milestone
			HashMap<User, Response> responseList = new HashMap<User, Response>();
			Milestone milestone = timeslot.getSchedule().getMilestone();
			ArrayList<String> requiredAttendees = milestone.getRequiredAttendees();
			for (String roleName : requiredAttendees) {
				Method roleGetter = Team.class.getDeclaredMethod("get" + roleName, null);
				Faculty roleUser = (Faculty) roleGetter.invoke(team, null);
				Response response = (overrideApproval) ? Response.APPROVED : Response.PENDING ;
				responseList.put(roleUser, response);
				reqAttendees.add(roleUser);
			}

			booking.setResponseList(responseList);
			booking.setRequiredAttendees(reqAttendees);
			booking.setLastEditedBy(user.getFullName());
			booking.setLastEditedAt(new Timestamp(Calendar.getInstance().getTimeInMillis()));
			if (!overrideApproval) { //Emails to be sent if normal process is followed
				NewBookingEmail newEmail = new NewBookingEmail(booking);
				RespondToBookingEmail responseEmail = new RespondToBookingEmail(booking);
				newEmail.sendEmail();
				responseEmail.sendEmail();
			} else { //Booking is automatically approved if process is bypassed
				ConfirmedBookingEmail confirmationEmail = new ConfirmedBookingEmail(booking);
				confirmationEmail.sendEmail();
			}

			em.persist(booking);

			//Setting the current active booking in the timeslot object
			timeslot.setCurrentBooking(booking);
			em.persist(timeslot);

			map.put("id", timeslot.getId());
			map.put("bookingId", booking.getId());
			map.put("datetime", dateFormat.format(timeslot.getStartTime()) + " " + timeFormat.format(timeslot.getStartTime()));
			map.put("time", viewTimeFormat.format(timeslot.getStartTime()) + " - " + viewTimeFormat.format(timeslot.getEndTime()));
			map.put("venue", timeslot.getVenue());
			map.put("team", team.getTeamName());
			map.put("teamId", team.getId());
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
				facultyMap.put("username", facultyUser.getUsername());
				facultyMap.put("status", statusList.get(facultyUser).toString());
				faculties.add(facultyMap);
			}
			map.put("faculties", faculties);

			TA ta = timeslot.getTA();
			String TA = (ta != null) ? ta.getFullName() : "-";
			map.put("TA", TA);
			map.put("wiki", team.getWiki());

			json.put("booking", map);
			MiscUtil.logActivity(logger, user, booking.toString() + " created");

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
			json.put("message", "Oops. Something went wrong on our end. Please try again!");
		}
		
		return json;
	}
	
	public synchronized static HashMap<String, Object> deleteBooking
			(EntityManager em, Timeslot ts, String comment,
			User user, ServletContext ctx)
			throws Exception
	{
		HashMap<String, Object> json = new HashMap<String, Object>();
		try {
			//set the current booking's status to deleted
			Booking b = ts.getCurrentBooking();
			b.setBookingStatus(BookingStatus.DELETED);
			b.setComment(comment);
			b.setLastEditedBy(user.getFullName());
			b.setLastEditedAt(new Timestamp(Calendar.getInstance().getTimeInMillis()));

			//set the current booking to null
			ts.setCurrentBooking(null);
			
			em.persist(b);
			em.persist(ts);

			//Forcing initialization for sending email
			Hibernate.initialize(b.getTeam().getMembers());
			Hibernate.initialize(b.getTimeslot().getSchedule().getMilestone());
			Hibernate.initialize(b.getRequiredAttendees());
			
			QuartzManager.deleteSMSReminder(b, ctx);

			//Sending email
			DeletedBookingEmail deletedEmail = new DeletedBookingEmail(b, user);
			deletedEmail.sendEmail();

			//if the booking has been removed successfully
			json.put("message", "Booking deleted successfully! All attendees have been notified via email.");
			json.put("success", true);
			json.put("bookingId", b.getId());

			MiscUtil.logActivity(logger, user, b.toString() + " deleted");	
		} catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
			json.put("message", "Oops. Something went wrong on our end. Please try again!");
		}
		
		return json;
	}
	
	public static void testSMS(long bookingId, HttpServletRequest request) throws Exception {
		StdSchedulerFactory factory = (StdSchedulerFactory) request.getSession()
				.getServletContext()
				.getAttribute(QuartzInitializerListener.QUARTZ_FACTORY_KEY);
		Scheduler scheduler = factory.getScheduler();
		JobDetail jd = JobBuilder.newJob(SMSReminderJob.class)
				.usingJobData("bookingId", bookingId)
				.withIdentity(String.valueOf(bookingId), MiscUtil.SMS_REMINDER_JOBS).build();
		//Calculating the time to trigger the job
		Calendar scheduledTime = Calendar.getInstance();
		scheduledTime.add(Calendar.SECOND, 10); //For testing
		Trigger tr = TriggerBuilder.newTrigger().withIdentity(String.valueOf(bookingId), MiscUtil.SMS_REMINDER_JOBS)
						.startAt(scheduledTime.getTime()).build();
		scheduler.scheduleJob(jd, tr);
	}
	
}
