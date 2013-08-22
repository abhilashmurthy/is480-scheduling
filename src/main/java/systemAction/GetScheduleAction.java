/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Response;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import manager.MilestoneManager;
import manager.ScheduleManager;
import manager.TermManager;
import model.Booking;
import model.Milestone;
import model.Schedule;
import model.Term;
import model.Timeslot;
import model.User;
import model.role.Student;
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
            SimpleDateFormat viewDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
            SimpleDateFormat viewTimeFormat = new SimpleDateFormat("HH:mm");
            
            Term term = TermManager.findByYearAndSemester(em, Integer.parseInt(academicYearString), semesterString);
            Milestone milestone = MilestoneManager.findByName(em, milestoneString);
            
            Schedule activeSchedule = ScheduleManager.findByTermAndMilestone(em, term, milestone);
            json.put("id", activeSchedule.getId());
            json.put("startDate", dateFormat.format(activeSchedule.getStartDate()));
            json.put("endDate", dateFormat.format(activeSchedule.getEndDate()));
            json.put("duration", milestone.getSlotDuration());
            
            ArrayList<HashMap<String, Object>> mapList = new ArrayList<HashMap<String, Object>>();
            logger.info("Timeslots size: " + activeSchedule.getTimeslots().size());
            for (Timeslot t : activeSchedule.getTimeslots()) {
                
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("id", t.getId());
                map.put("datetime", dateFormat.format(t.getStartTime()) + " " + timeFormat.format(t.getStartTime()));

                //Getting venue for timeslot
                String venue = t.getVenue();
                map.put("venue", venue);
                
                if (t.getCurrentBooking()!= null) {
					Booking b = t.getCurrentBooking();
                    Date startDate = new Date(t.getStartTime().getTime());
                    Date endDate = new Date(t.getEndTime().getTime());
                    map.put("team", b.getTeam().getTeamName());
                    
                    //View start date (DDD, dd MMM YYYY)
                    map.put("startDate", viewDateFormat.format(startDate));
                    
                    //Overall status
                    map.put("status", b.getBookingStatus().toString());
                    
                    //Start Time - End Time
                    map.put("time", viewTimeFormat.format(startDate) + " - " + viewTimeFormat.format(endDate));

                    //This list contains all the attendees for the timeslot (Team Members, Supervisors, Reviewers)
                    List<HashMap<String, String>> students = new ArrayList<HashMap<String, String>>();
                    List<HashMap<String, String>> faculties = new ArrayList<HashMap<String, String>>();

                    //Adding all students
                    Set<Student> teamMembers = b.getTeam().getMembers();
                    for (User student : teamMembers) {
                        HashMap<String, String> studentMap = new HashMap<String, String>();
                        studentMap.put("name", student.getFullName());
                        students.add(studentMap);
                    }
                    
                    //Adding all faculty and their status
                    HashMap<User, Response> statusList = b.getResponseList();
                    if (statusList != null) {
                        for (User faculty : statusList.keySet()) {
                            HashMap<String, String> facultyMap = new HashMap<String, String>();
                            facultyMap.put("name", faculty.getFullName());
                            facultyMap.put("status", statusList.get(faculty).toString());
                            faculties.add(facultyMap);
                        }
                    }
                    
                    //Setting the list of attendees
                    map.put("students", students);
                    map.put("faculties", faculties);

                    //TODO: Things this code cannot get as of now (can only do this when database has values)
                    String TA = "-";
                    map.put("TA", TA);
                    String teamWiki = "-";
                    map.put("teamWiki", teamWiki);
                }
                mapList.add(map);
            }
            logger.info("mapList size: " + mapList.size());
            json.put("timeslots", mapList);
            json.put("success", true);
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
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
