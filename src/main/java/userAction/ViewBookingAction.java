/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Status;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.TimeslotManager;
import model.Team;
import model.Timeslot;
import model.User;
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
    private String timeslotId;
//	private ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    private HashMap<String, Object> json = new HashMap<String, Object>();

    @Override
    public String execute() throws Exception {
        try {
            EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
            //convert the chosen ID into long
            long chosenID = Long.parseLong(timeslotId);
            Timeslot ts = TimeslotManager.findById(em, chosenID);

            //Check whether timeslot exists
            if (ts != null) {
                //Check whether timeslot has a team assigned to it
                if (ts.getTeam() != null) {
                    Timestamp st = ts.getStartTime();
                    Timestamp et = ts.getEndTime();
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
                    String teamName = ts.getTeam().getTeamName();
                    json.put("teamName", teamName);

                    //This list contains all the attendees for the timeslot (Team Members, Supervisors, Reviewers)
                    List<HashMap<String, String>> attendees = new ArrayList<HashMap<String, String>>();
                    String finalStatus = "error";

                    //Getting all the team members associated with the timeslot
                    //First getting the team members
                    Team team = ts.getTeam();
                    if (team != null) {
                        Set<User> teamMembers = team.getMembers();
                        Iterator it = teamMembers.iterator();
                        while (it.hasNext()) {
                            HashMap<String, String> userMap = new HashMap<String, String>();
                            User teamMember = (User) it.next();
                            userMap.put("name", teamMember.getFullName());

                            attendees.add(userMap);
                        }
                    }

                    //Second getting the supervisor/reviewer for the timeslot
                    HashMap<User, Status> members = null;
                    if (ts.getStatusList() != null) {
                        members = ts.getStatusList();
                        Iterator iter = members.keySet().iterator();
                        while (iter.hasNext()) {
                            HashMap<String, String> userMap = new HashMap<String, String>();
                            User supervisorReviewer = (User) iter.next();
                            Status status = members.get(supervisorReviewer);
                            userMap.put("name", supervisorReviewer.getFullName());
                            userMap.put("status", status.toString());
                            if (status.equals(Status.ACCEPTED)) {
                                finalStatus = "Accepted";
                            }
                            if (status.equals(Status.REJECTED)) {
                                finalStatus = "Rejected";
                            }
                            if (!finalStatus.equals("Rejected") && status.equals(Status.PENDING)) {
                                finalStatus = "Pending";
                            }
                            attendees.add(userMap);
                        }
                    }
                    //Setting the list of attendees
                    json.put("attendees", attendees);
                    
                    //Setting the final status of the booking
                    json.put("status", finalStatus);

                    //Getting venue for timeslot
                    String venue = ts.getVenue();
                    json.put("venue", venue);

                    //Things this code cannot get as of now (can only do this when database has values)
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
        }
    }

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }

    public String getTimeslotId() {
        return timeslotId;
    }

    public void setTimeslotId(String timeslotId) {
        this.timeslotId = timeslotId;
    }

    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }
} //end of class