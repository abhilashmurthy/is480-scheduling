/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Response;
import constant.Role;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import model.Schedule;
import model.Timeslot;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import manager.BookingManager;
import model.Booking;
import model.Term;
import model.role.Faculty;
import model.role.Student;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class BookingHistoryAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(BookingHistoryAction.class);
    private ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    private HashMap<String, Object> json = new HashMap<String, Object>();

    @Override
    public String execute() throws Exception {
		EntityManager em = null;
        try {
            em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
            HttpSession session = request.getSession();
            //Getting the active role of the user
            Role activeRole = (Role) session.getAttribute("activeRole");
			
			//Setting updated user object in session
			//For updating user object after booking is creating (Status update & delete booking code already present in respective files)
			em.clear();
			User oldUser = (User) session.getAttribute("user");
			User user = em.find(User.class, oldUser.getId());
			session.setAttribute("user", user);
				
			/* Getting active term. Initially, only bookings for active term will be displayed to the user. 
			 * If the user wishes to view bookin history for another term, he/she will have to change the 
			 * active term.
			*/
			Term activeTerm = (Term) session.getAttribute("currentActiveTerm");
			
			Set<Booking> bookings = null;
			//Retrieving all bookings for admin and course coordinator
			if (activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)) {
				//Getting all the bookings for the active term
				ArrayList<Booking> bookingsList = BookingManager.getBookingsByTerm(em, activeTerm);
				//Converting list to set (Duplicated values will be ignored)
				if (bookingsList != null) {
					bookings = new HashSet(bookingsList);
				}
				
			} else if (activeRole.equals(Role.TA)) {
				/* TO DO after TA Model class is ready */
				// TA ta = (TA) session.getAttribute("user");
				// bookings = ta.getSubscribedBookings();
			} else {
				
				Faculty faculty = null;
				Student student = null;
				if (activeRole.equals(Role.FACULTY)) {
					faculty = (Faculty) session.getAttribute("user");
					bookings = faculty.getRequiredBookings();
				} else if (activeRole.equals(Role.STUDENT)) {
					student = (Student) session.getAttribute("user");
					bookings = student.getRequiredBookings();
					for (Booking b: bookings) {
						Term term = b.getTimeslot().getSchedule().getMilestone().getTerm();
					}
				}
			}
				
			//Iterating over the list and getting the necessary details
			if (bookings != null && bookings.size() > 0) {
				for (Booking b: bookings) {
					Timeslot timeslot = b.getTimeslot();
					HashMap<String, Object> map = new HashMap<String, Object>();
					SimpleDateFormat sdfForDate = new SimpleDateFormat("MMM dd yyyy, EEE");
					SimpleDateFormat sdfForTime = new SimpleDateFormat("HH:mm aa");

					String venue = timeslot.getVenue();
					String teamName = b.getTeam().getTeamName();
					//Getting the schedule based on timeslot (Each timeslot belongs to 1 unique schedule)
					Schedule schedule = timeslot.getSchedule();
					String milestoneName = "";
					if (schedule != null) {
						milestoneName = schedule.getMilestone().getName();
					}
					String date = sdfForDate.format(timeslot.getStartTime());
					String time = sdfForTime.format(timeslot.getStartTime()) + " - " + 
							sdfForTime.format(timeslot.getEndTime());

					//Only for supervisors/reviewers (or Faculty)
					if (activeRole.equals(Role.FACULTY)) { 
						Faculty faculty = (Faculty) session.getAttribute("user");
						String myStatus = b.getResponseList().get(faculty).toString();
						map.put("myStatus", myStatus);
					}

					//Overall status (will be seen by all stakeholders)
					String overallBookingStatus = b.getBookingStatus().toString();
					map.put("overallBookingStatus", overallBookingStatus);

					if (activeRole.equals(Role.STUDENT) || activeRole.equals(Role.ADMINISTRATOR) 
							|| activeRole.equals(Role.COURSE_COORDINATOR)) {
						//Detailed status
						List<HashMap<String, String>> individualStatusList = new ArrayList<HashMap<String, String>>();
						if (b.getResponseList() != null) {
							for (Entry<User, Response> e: b.getResponseList().entrySet()) {
								HashMap<String, String> userMap = new HashMap<String, String>();
								userMap.put("name", e.getKey().getFullName());
								userMap.put("status", e.getValue().toString());

								individualStatusList.add(userMap);
							}
						}
						map.put("individualBookingStatus", individualStatusList);
					}

					map.put("teamName", teamName);
					map.put("milestone", milestoneName);
					map.put("date", date);
					map.put("time", time);
					map.put("venue", venue);

					data.add(map);
				}
			}
			json.put("success", true);
			return SUCCESS;

		} catch (Exception e) {
//            logger.error("Exception caught: " + e.getMessage());
//            if (MiscUtil.DEV_MODE) {
//                for (StackTraceElement s : e.getStackTrace()) {
//                    logger.debug(s.toString());
//                }
//            }
//            json.put("success", false);
//            json.put("message", "Error with BookingHistory: Escalate to developers!");
			request.setAttribute("error", "Error with BookingHistory: Escalate to developers!");
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}
        return ERROR;
    } //end of execute function

    public ArrayList<HashMap<String, Object>> getData() {
        return data;
    }

    public void setData(ArrayList<HashMap<String, Object>> data) {
        this.data = data;
    }

    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }
} //end of class