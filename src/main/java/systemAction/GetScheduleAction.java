/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Status;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import manager.MilestoneManager;
import manager.ScheduleManager;
import manager.TermManager;
import model.Milestone;
import model.Schedule;
import model.Team;
import model.Term;
import model.Timeslot;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class GetScheduleAction extends ActionSupport implements ServletRequestAware {
    
    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(GetScheduleAction.class);
    private final boolean debugMode = true;
    private String milestoneString;
    private String academicYearString;
    
    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
    
    public String getAcademicYearString() {
        return academicYearString;
    }
    
    public void setAcademicYearString(String academicYearString) {
        this.academicYearString = academicYearString;
    }
    
    public String getSemesterString() {
        return semesterString;
    }
    
    public void setSemesterString(String semesterString) {
        this.semesterString = semesterString;
    }
    private String semesterString;
    
    public String getMilestoneString() {
        return milestoneString;
    }
    
    public void setMilestoneString(String milestoneString) {
        this.milestoneString = milestoneString;
    }
    
    @Override
    public String execute() throws ServletException, IOException {
        try {
            EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
            
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            Term term = TermManager.findByYearAndSemester(em, Integer.parseInt(academicYearString), semesterString);
            Milestone milestone = MilestoneManager.findByName(em, milestoneString);
            
            Schedule activeSchedule = ScheduleManager.findByTermAndMilestone(em, term, milestone);
            json.put("startDate", dateFormat.format(activeSchedule.getStartDate()));
            json.put("endDate", dateFormat.format(activeSchedule.getEndDate()));
            json.put("duration", milestone.getSlotDuration());
            
            ArrayList<HashMap<String, Object>> mapList = new ArrayList<HashMap<String, Object>>();
            for (Timeslot t : activeSchedule.getTimeslots()) {
                
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("id", t.getId());
                map.put("datetime", dateFormat.format(t.getStartTime()) + " " + timeFormat.format(t.getStartTime()));

                //Getting venue for timeslot
                String venue = t.getVenue();
                map.put("venue", venue);
                
                if (t.getTeam() != null) {
                    map.put("team", t.getTeam().getTeamName());

                    //This list contains all the attendees for the timeslot (Team Members, Supervisors, Reviewers)
                    List<HashMap<String, String>> attendees = new ArrayList<HashMap<String, String>>();

                    //Getting all the team members associated with the timeslot
                    //First getting the team members
                    Team team = t.getTeam();
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
                    if (t.getStatusList() != null) {
                        members = t.getStatusList();
                        Iterator iter = members.keySet().iterator();
                        while (iter.hasNext()) {
                            HashMap<String, String> userMap = new HashMap<String, String>();
                            User supervisorReviewer = (User) iter.next();
                            Status status = members.get(supervisorReviewer);
                            userMap.put("name", supervisorReviewer.getFullName());
                            userMap.put("status", status.toString());
                            
                            attendees.add(userMap);
                        }
                    }
                    //Setting the list of attendees
                    map.put("attendees", attendees);

                    //Things this code cannot get as of now (can only do this when database has values)
                    String TA = "-";
                    map.put("TA", TA);
                    String teamWiki = "-";
                    map.put("teamWiki", teamWiki);
                }
                mapList.add(map);
            }
            json.put("timeslots", mapList);
            json.put("success", true);
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (debugMode) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("message", "Error with GetSchedule: Escalate to developers!");
        }
        return SUCCESS;
    }
    private HashMap<String, Object> json = new HashMap<String, Object>();
    
    public HashMap<String, Object> getJson() {
        return json;
    }
    
    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }
}
