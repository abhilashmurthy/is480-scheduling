/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Response;
import constant.Role;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.TimeslotManager;
import model.Booking;
import model.Schedule;
import model.SystemActivityLog;
import model.Timeslot;
import model.User;
import model.role.Student;
import notification.email.EditBookingEmail;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.hibernate.Hibernate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Tarlochan
 */
public class UpdateBookingAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(UpdateBookingAction.class);
    private HashMap<String, Object> json = new HashMap<String, Object>();

    @Override
    public String execute() throws ServletException, IOException {
		HttpSession session = request.getSession();
		
		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());
		
		SystemActivityLog logItem = new SystemActivityLog();
		logItem.setActivity("Administrator: Update Booking Details");
		logItem.setRunTime(now);
		logItem.setUser((User)session.getAttribute("user"));
		logItem.setMessage("Error with validation / No changes made");
		logItem.setSuccess(true);
		
        EntityManager em = null;
        try {
            //Code here
            //convert the chosen ID into long and get the corresponding Timeslot object
            em = MiscUtil.getEntityManagerInstance();
			
			em.getTransaction().begin();

            //Checking whether the user setting optional attendees is student, admin or cc
            User user = (User) session.getAttribute("user");
            if (!user.getRole().equals(Role.TA)) {

                //Getting input data from url
                JSONObject inputData = (JSONObject) new JSONObject(request.getParameter("jsonData"));
                long chosenID = Long.parseLong(inputData.getString("timeslotId"));
                Timeslot oldTimeslot = TimeslotManager.findById(em, chosenID);

                //Update booking parameters
                Timestamp newBookingTimestamp = null;
                HashSet<String> optionalAttendees = null;
                String newVenue = null;
                try {
                    newVenue = inputData.getString("newVenue");
                } catch (JSONException j) {
                    logger.debug("newVenue not found");
                }
                
                //Parse Timestamp
                try {
                    String newDateTime = inputData.getString("newDateTime");
                    if (newDateTime != null && !newDateTime.equals("")) {
                        newBookingTimestamp = Timestamp.valueOf(newDateTime);
                    }
                } catch (JSONException j) {
                    logger.debug("newDateTime not found");
                } catch (Exception e) {
                    json.put("success", false);
                    json.put("message", "Start Date and Time in the wrong format!");
                    return SUCCESS;
                }
                
                //Parse Optional Attendees
                try {
                    //Getting the optional attendees and storing in a list
                    JSONArray optionalAttendeesArray = (JSONArray) inputData.getJSONArray("attendees");
                    optionalAttendees = new HashSet<String>();
                    StringBuilder invalidEmails = new StringBuilder();

                    for (int i = 0; i < optionalAttendeesArray.length(); i++) {
                        String attendeeEmailAddress = optionalAttendeesArray.getString(i);
                        //Validating the email address string
                        try {
                            InternetAddress emailAddr = new InternetAddress(attendeeEmailAddress);
                            emailAddr.validate();
                            optionalAttendees.add(attendeeEmailAddress);
                        } catch (AddressException ex) {
                            logger.debug("Invalid email: " + attendeeEmailAddress);
                            invalidEmails.append("- ").append(attendeeEmailAddress).append("\n");
                        }
                    }
                    if (invalidEmails.length() > 0) {
                        json.put("success", false);
                        json.put("message", "Invalid email addresses detected: \n" + invalidEmails.toString());
                        return SUCCESS;
                    }
                } catch (JSONException j) {
                    //No optional attendees json found
                    optionalAttendees = new HashSet<String>();
                    logger.error("Erm should not reach here");
                }

                //------------To update the booking date and start time----------------------
                //update timeslot and change it based on date
                //go through timeslot to compare start time with the user
                //suggested start time and see if the timeslot is taken.
                
                //Old Timeslot details
                Schedule scheduleOfBooking = oldTimeslot.getSchedule();
                Booking booking = oldTimeslot.getCurrentBooking();
                
                //Return parameters for Update Booking
                HashMap<String, Object> map = new HashMap<String, Object>();
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat viewDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
                SimpleDateFormat viewTimeFormat = new SimpleDateFormat("HH:mm");

                map.put("id", oldTimeslot.getId());
                map.put("datetime", dateFormat.format(oldTimeslot.getStartTime()) + " " + timeFormat.format(oldTimeslot.getStartTime()));
                map.put("time", viewTimeFormat.format(oldTimeslot.getStartTime()) + " - " + viewTimeFormat.format(oldTimeslot.getEndTime()));
                map.put("venue", oldTimeslot.getVenue());
                map.put("team", booking.getTeam().getTeamName());
                map.put("startDate", viewDateFormat.format(new Date(oldTimeslot.getStartTime().getTime())));
                map.put("status", booking.getBookingStatus().toString());
                List<HashMap<String, String>> students = new ArrayList<HashMap<String, String>>(); //Adding all students
                Set<Student> teamMembers = booking.getTeam().getMembers();
                for (User studentUser : teamMembers) {
                    HashMap<String, String> studentMap = new HashMap<String, String>();
                    studentMap.put("name", studentUser.getFullName());
                    students.add(studentMap);
                }
                map.put("students", students);
                List<HashMap<String, String>> faculties = new ArrayList<HashMap<String, String>>(); //Adding all faculty and their status
                HashMap<User, Response> statusList = booking.getResponseList();
                for (User facultyUser : statusList.keySet()) {
                    HashMap<String, String> facultyMap = new HashMap<String, String>();
                    facultyMap.put("name", facultyUser.getFullName());
                    facultyMap.put("status", statusList.get(facultyUser).toString());
                    faculties.add(facultyMap);
                }
                map.put("faculties", faculties);
                Set<String> oldOptionals = booking.getOptionalAttendees();
                List<HashMap<String, String>> optionals = new ArrayList<HashMap<String, String>>(); //Adding all students
                for (String oldOptional : oldOptionals) {
                    HashMap<String, String> optionalMap = new HashMap<String, String>();
                    optionalMap.put("id", oldOptional);
                    optionalMap.put("name", oldOptional);
                    optionals.add(optionalMap);
                }
                map.put("optionals", optionals);
                
                String TA = "-";
                map.put("TA", TA);
                String teamWiki = "-";
                map.put("teamWiki", teamWiki);
                
                //Return if no change detected
                if ((oldTimeslot.getStartTime().equals(newBookingTimestamp)
                        && optionalAttendees.equals(booking.getOptionalAttendees())
                        && oldTimeslot.getVenue().equals(newVenue))
                        ||
                        (newBookingTimestamp == null
                            && optionalAttendees.equals(booking.getOptionalAttendees())
                            && oldTimeslot.getVenue().equals(newVenue))
                        ||
                        (newVenue == null
                            && optionalAttendees.equals(booking.getOptionalAttendees())
                            && oldTimeslot.getStartTime().equals(newBookingTimestamp))
                        ||
                        (newVenue == null
                            && newBookingTimestamp == null
                            && optionalAttendees.equals(booking.getOptionalAttendees()))) {
                    json.put("success", false);
                    json.put("message", "No change made.. ");
                    return SUCCESS;
                }
                
                //New venue
                if (newVenue != null) {
                    oldTimeslot.setVenue(newVenue);
                    map.put("venue", oldTimeslot.getVenue());
                }
                
                //New optional attendees
                if (!optionalAttendees.equals(booking.getOptionalAttendees())) {
                    booking.setOptionalAttendees(optionalAttendees);
                    List<HashMap<String, String>> newOptionals = new ArrayList<HashMap<String, String>>(); //Adding all students
                    for (String optionalAttendee : optionalAttendees) {
                        HashMap<String, String> optionalMap = new HashMap<String, String>();
                        optionalMap.put("id", optionalAttendee);
                        optionalMap.put("name", optionalAttendee);
                        newOptionals.add(optionalMap);
                    }
                    map.put("optionals", newOptionals);
                }
                
                //New Timestamp
                if (newBookingTimestamp != null && !oldTimeslot.getStartTime().equals(newBookingTimestamp)) {
                    Timeslot newTimeslot = TimeslotManager.getByTimestampAndSchedule(em, newBookingTimestamp, scheduleOfBooking);
                    if (newTimeslot == null) {
                        json.put("success", false);
                        json.put("message", "That timeslot does not exist!");
                        return SUCCESS;
                    } else if (newTimeslot.getCurrentBooking() != null) { //TODO: Handle this differently for multiple bookings in one timeslot
                        json.put("success", false);
                        json.put("message", "Another team already booked that slot");
                        return SUCCESS;
                    }
                    //Update booking details
                    newTimeslot.setVenue(oldTimeslot.getVenue());
                    booking.setTimeslot(newTimeslot);
                    oldTimeslot.setCurrentBooking(null);
                    newTimeslot.setCurrentBooking(booking);
                    
                    map.put("id", newTimeslot.getId());
                    map.put("datetime", dateFormat.format(newTimeslot.getStartTime()) + " " + timeFormat.format(newTimeslot.getStartTime()));
                    map.put("time", viewTimeFormat.format(newTimeslot.getStartTime()) + " - " + viewTimeFormat.format(newTimeslot.getEndTime()));
                    map.put("venue", newTimeslot.getVenue());
                    map.put("startDate", viewDateFormat.format(new Date(newTimeslot.getStartTime().getTime())));

                    //Begin database changes
                    em.persist(newTimeslot);
                    em.persist(oldTimeslot);
                }
                
                booking.setLastEditedBy(user.getFullName());
                booking.setLastEditedAt(new Timestamp(Calendar.getInstance().getTimeInMillis()));
                
                em.persist(booking);
				
				//Forcing initialization for sending email
				Hibernate.initialize(booking.getTeam().getMembers());
				Hibernate.initialize(booking.getTimeslot().getSchedule().getMilestone());
				
				//Sending email update
				EditBookingEmail email = new EditBookingEmail(booking, user);
				email.sendEmail();
                em.getTransaction().commit();
                
                json.put("booking", map);
                json.put("success", true);
                json.put("message", "Booking updated successfully!");
				MiscUtil.logActivity(logger, user, booking.toString() + " updated");
				
				logItem.setMessage("Booking details updated successfully for " + booking.toString());
            } else {
                //Incorrect user role
                json.put("success", false);
                json.put("message", "You are not authorized to update the booking!");
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
            request.setAttribute("error", "Error with UpdateBooking: Escalate to developers!");
            json.put("success", false);
            json.put("message", "Error with UpdateBooking: Escalate to developers!");
            return ERROR;
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
    } //end of execute

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }

    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }
}
