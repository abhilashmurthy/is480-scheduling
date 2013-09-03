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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.ScheduleManager;
import manager.TimeslotManager;
import model.Booking;
import model.Schedule;
import model.Timeslot;
import model.User;
import model.role.Student;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONArray;
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
        EntityManager em = null;
        try {
            //Code here
            //convert the chosen ID into long and get the corresponding Timeslot object
            em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
			HttpSession session = request.getSession();
			
			//Checking whether the user setting optional attendees is student, admin or cc
			User user = (User) session.getAttribute("user");
			if (user.getRole().equals(Role.ADMINISTRATOR) || user.getRole().equals(Role.COURSE_COORDINATOR) ||
					user.getRole().equals(Role.STUDENT)) {
				
				//Getting input data from url
				JSONObject inputData = (JSONObject) new JSONObject (request.getParameter("jsonData"));
				long chosenID = Long.parseLong(inputData.getString("timeslotId"));
				Timeslot ts = TimeslotManager.findById(em, chosenID);

				//JSON Return for updated booking
				HashMap<String, Object> map = new HashMap<String, Object>();
				SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat viewDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
				SimpleDateFormat viewTimeFormat = new SimpleDateFormat("HH:mm");

				//get all availabe timeslots
				List<Timeslot> allSlots = TimeslotManager.getAllTimeslots(em);

				Timestamp newbookingTime = null;

				//Check whether the edited start date and time is in the correct format
				try {
					String changedDate = inputData.getString("changedDate");
					if (changedDate != null && !changedDate.equals("")) {
						newbookingTime = Timestamp.valueOf(changedDate);
					}
				} catch (Exception e) {
					json.put("success", false);
					json.put("message", "Start Date and Time in the wrong format!");
					return SUCCESS;
				}
			
				//Getting the optional attendees and storing in a list
				JSONArray optionalAttendeesArray = (JSONArray) inputData.getJSONArray("attendees");
				//Constructing the optional attendees list to be stored in db
				HashSet<String> optionalAttendees = new HashSet<String>();

				for (int i = 0; i < optionalAttendeesArray.length(); i++) {
					String attendeeEmailAddress = optionalAttendeesArray.getString(i);
					//Validating the email address string
					boolean result = false;
					try {
						InternetAddress emailAddr = new InternetAddress(attendeeEmailAddress);
						emailAddr.validate();
						result = true;
					} catch (AddressException ex) {
						result = false;
					}
					if (result == false) {
						json.put("error", true);
						json.put("message", "1 or more email addresses are invalid!");
						return SUCCESS;
					}
					//Any duplicate email address will be replaced (due to hash set)
					optionalAttendees.add(attendeeEmailAddress);
				}
			
				//------------To update the booking date and start time----------------------
				//update timeslot and change it based on date
				//go through timeslot to compare start time with the user
				//suggested start time and see if the timeslot is taken.
				//if the timeslot is not assigned to any team, update timeslot to
				//the new timeslot
				if (newbookingTime != null) {
					boolean successUpdate = false;

					//get the present timeslot's schedule id
					Schedule scheduleOfBooking = ScheduleManager.findById(em, ts.getSchedule().getId());
					String errorMsg = "That timeslot does not exist";
					for (Timeslot toCompare : allSlots) {
						if (toCompare.getStartTime().equals(newbookingTime) && scheduleOfBooking == toCompare.getSchedule()) {
							if (toCompare.getCurrentBooking() != null) {
								errorMsg = "Another team already booked that slot";
								break;
							}

							em.getTransaction().begin();

							//set the new timeslot to team
							toCompare.setCurrentBooking(ts.getCurrentBooking());

							//change the timeslot_id of booking to the new timeslot
							Booking booking = ts.getCurrentBooking();
							booking.setTimeslot(toCompare);

							map.put("id", toCompare.getId());
							map.put("datetime", dateFormat.format(toCompare.getStartTime()) + " " + timeFormat.format(toCompare.getStartTime()));
							map.put("time", viewTimeFormat.format(toCompare.getStartTime()) + " - " + viewTimeFormat.format(toCompare.getEndTime()));
							map.put("venue", toCompare.getVenue());
							map.put("team", ts.getCurrentBooking().getTeam().getTeamName());
							map.put("startDate", viewDateFormat.format(new Date(toCompare.getStartTime().getTime())));
							map.put("status", ts.getCurrentBooking().getBookingStatus().toString());

							//Adding all students
							List<HashMap<String, String>> students = new ArrayList<HashMap<String, String>>();
							Set<Student> teamMembers = ts.getCurrentBooking().getTeam().getMembers();
							for (User studentUser : teamMembers) {
								HashMap<String, String> studentMap = new HashMap<String, String>();
								studentMap.put("name", studentUser.getFullName());
								students.add(studentMap);
							}
							map.put("students", students);

							//Adding all faculty and their status
							List<HashMap<String, String>> faculties = new ArrayList<HashMap<String, String>>();
							HashMap<User, Response> statusList = ts.getCurrentBooking().getResponseList();
							for (User facultyUser : statusList.keySet()) {
								HashMap<String, String> facultyMap = new HashMap<String, String>();
								facultyMap.put("name", facultyUser.getFullName());
								facultyMap.put("status", statusList.get(facultyUser).toString());
								faculties.add(facultyMap);
							}
							map.put("faculties", faculties);
							String TA = "-";
							map.put("TA", TA);
							String teamWiki = "-";
							map.put("teamWiki", teamWiki);
							json.put("booking", map);

							//Setting the old timeslot to null
							ts.setCurrentBooking(null);

							//Setting optional attendees for the booking
							if (optionalAttendees.size() > 0) {
								booking.setOptionalAttendees(optionalAttendees);
							}

							em.persist(toCompare);
							em.persist(ts);
							em.persist(booking);
							em.getTransaction().commit();
							
							successUpdate = true;
							break;
						}
					}	
					if (!successUpdate) {
						json.put("success", false);
						json.put("message", errorMsg);
					} else {
						json.put("success", true);
						json.put("message", "Booking updated successfully!");
					}
				} else {
					//This is the case when a student adds optional attendees
					if (optionalAttendees.size() > 0) {
						Booking b = ts.getCurrentBooking();
						if (b != null) {
							em.getTransaction().begin();
							b.setOptionalAttendees(optionalAttendees);
							em.persist(b);
							em.getTransaction().commit();

							json.put("success", true);
							json.put("message", "Attendees have been invited for your booking!");
						} else {
							json.put("error", true);
							json.put("message", "No booking has been made at this timeslot!");
							return SUCCESS;
						}
					}
				}
			} else {
				//Incorrect user role
				json.put("error", true);
				json.put("message", "You are not authorized to update the booking!");
			}
			return SUCCESS;
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            request.setAttribute("error", "Error with UpdateBooking: Escalate to developers!");
            return ERROR;
        } finally {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (em != null && em.isOpen()) {
                em.close();
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
