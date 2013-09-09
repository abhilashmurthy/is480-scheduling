/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Response;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import model.Booking;
import model.Team;
import model.User;
import model.role.Student;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Tarlochan
 */
public class ViewBookingAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(ViewBookingAction.class);
    private String bookingId;
//	private ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    private HashMap<String, Object> json = new HashMap<String, Object>();

    @Override
    public String execute() throws Exception {
		EntityManager em = null;
        try {
            em = MiscUtil.getEntityManagerInstance();
            //convert the chosen ID into long
            long chosenID = Long.parseLong(bookingId);
            Booking b = em.find(Booking.class, chosenID);

            //Check whether timeslot exists
            if (b != null) {
                //Check whether timeslot has a team assigned to it
                if (b.getTeam() != null) {
                    Timestamp st = b.getTimeslot().getStartTime();
                    Timestamp et = b.getTimeslot().getEndTime();
                    Date startDate = new Date(st.getTime());
                    Date endDate = new Date(et.getTime());

                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

                    //Converting the start date of timeslot
                    String dateSlot = dateFormat.format(startDate);
                    json.put("startDate", dateSlot);
                    //Converting the start time & end time of timeslot
                    String startTimeString = timeFormat.format(startDate);
                    json.put("startTime", startTimeString);
                    String endTimeString = timeFormat.format(endDate);
                    json.put("endTime", endTimeString);

                    //Get team's name from the timeslot
                    String teamName = b.getTeam().getTeamName();
                    json.put("teamName", teamName);

                    //This list contains all the attendees for the timeslot (Team Members, Supervisors, Reviewers)
                    List<HashMap<String, String>> attendees = new ArrayList<HashMap<String, String>>();

                    //Getting all the team members associated with the timeslot
                    //First getting the team members
                    Team team = b.getTeam();
                    if (team != null) {
                        Set<Student> teamMembers = team.getMembers();
                        Iterator it = teamMembers.iterator();
                        while (it.hasNext()) {
                            HashMap<String, String> userMap = new HashMap<String, String>();
                            User teamMember = (User) it.next();
                            userMap.put("name", teamMember.getFullName());

                            attendees.add(userMap);
                        }
                    }

                    //Second getting the supervisor/reviewer for the timeslot
                    if (b.getResponseList() != null) {
                        for (Entry<User, Response> e : b.getResponseList().entrySet()) {
                            HashMap<String, String> userMap = new HashMap<String, String>();
                            userMap.put("name", e.getKey().getFullName());
                            userMap.put("status", e.getValue().toString());
                            attendees.add(userMap);
                        }
                    }
                    //Setting the list of attendees
                    json.put("attendees", attendees);
                    
                    //Setting the final status of the booking
                    json.put("status", b.getBookingStatus());

                    //Getting venue for timeslot
                    String venue = b.getTimeslot().getVenue();
                    json.put("venue", venue);

                    //TODO Things this code cannot get as of now (can only do this when database has values)
                    String TA = "-";
                    json.put("TA", TA);
                    String teamWiki = "-";
                    json.put("teamWiki", teamWiki);

                    json.put("success", true);
                    return SUCCESS;
                }
                json.put("error", true);
                json.put("message", "This booking is empty!");
                json.put("success", true);
                return SUCCESS;
            }
            json.put("error", true);
            json.put("message", "This booking doesnt exist!");
            json.put("success", true);
            return SUCCESS;
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("message", "Error with UpdateSchedule: Escalate to developers!");
            json.put("success", false);
            return SUCCESS;
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}
    }

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }

    public String getTimeslotId() {
        return bookingId;
    }

    public void setTimeslotId(String timeslotId) {
        this.bookingId = timeslotId;
    }

    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }
} //end of class