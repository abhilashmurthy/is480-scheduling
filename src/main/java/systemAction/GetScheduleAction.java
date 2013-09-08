/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Response;
import constant.Role;
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
import javax.servlet.http.HttpSession;
import manager.MilestoneManager;
import manager.ScheduleManager;
import model.Booking;
import model.Milestone;
import model.Schedule;
import model.Team;
import model.Term;
import model.Timeslot;
import model.User;
import model.role.Faculty;
import model.role.Student;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Hibernate;
import org.json.JSONObject;
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
    private String milestone;
    private String year;

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }
    private String semester;

    public String getMilestone() {
        return milestone;
    }

    public void setMilestone(String milestone) {
        this.milestone = milestone;
    }

    @Override
    public String execute() throws ServletException, IOException {
        EntityManager em = null;
        try {
            em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat viewDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
            SimpleDateFormat viewTimeFormat = new SimpleDateFormat("HH:mm");
            
            HttpSession session = request.getSession();
            Term term = (Term) session.getAttribute("currentActiveTerm");
            
            if (milestone != null) {
                //Return schedule data by milestone
                Milestone milestoneObject = MilestoneManager.findByNameAndTerm(em, milestone, term);
                Student student = null;
                Faculty faculty = null;

                Schedule activeSchedule = ScheduleManager.findByMilestone(em, milestoneObject);
                json.put("id", activeSchedule.getId());
                json.put("milestoneId", milestoneObject.getId());
                json.put("startDate", dateFormat.format(activeSchedule.getStartDate()));
                json.put("endDate", dateFormat.format(activeSchedule.getEndDate()));
                json.put("duration", milestoneObject.getSlotDuration());
                json.put("dayStartTime", activeSchedule.getDayStartTime());
                json.put("dayEndTime", activeSchedule.getDayEndTime());

                //Get unavailable timeslots if user is a student
                Set<Timeslot> supervisorAvailability = null;
                Set<Timeslot> reviewer1Availability = null;
                Set<Timeslot> reviewer2Availability = null;
                User user = (User) request.getSession().getAttribute("user");
                if (user.getRole() == Role.STUDENT) {
                    student = em.find(Student.class, user.getId());
                    Team team = student.getTeam();
                    supervisorAvailability = team.getSupervisor().getUnavailableTimeslots();
                    reviewer1Availability = team.getReviewer1().getUnavailableTimeslots();
                    reviewer2Availability = team.getReviewer2().getUnavailableTimeslots();
                } else if (user.getRole() == Role.FACULTY) {
                    faculty = em.find(Faculty.class, user.getId());
                    supervisorAvailability = faculty.getUnavailableTimeslots();
                }

                ArrayList<HashMap<String, Object>> mapList = new ArrayList<HashMap<String, Object>>();
                logger.info("Timeslots size: " + activeSchedule.getTimeslots().size());
                for (Timeslot t : activeSchedule.getTimeslots()) {

                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("id", t.getId());
                    map.put("datetime", dateFormat.format(t.getStartTime()) + " " + timeFormat.format(t.getStartTime()));

                    //Getting venue for timeslot
                    String venue = t.getVenue();
                    map.put("venue", venue);

                    if (t.getCurrentBooking() != null) {
                        Booking b = t.getCurrentBooking();
                        boolean isMyTeam = false;
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
                        List<HashMap<String, String>> optionals = new ArrayList<HashMap<String, String>>();

                        //Adding all students
                        Set<Student> teamMembers = b.getTeam().getMembers();
                        for (User studentUser : teamMembers) {
                            HashMap<String, String> studentMap = new HashMap<String, String>();
                            studentMap.put("name", studentUser.getFullName());
                            students.add(studentMap);
                        }

                        //Adding all faculty and their status
                        HashMap<User, Response> statusList = b.getResponseList();
                        if (statusList != null) {
                            for (User facultyUser : statusList.keySet()) {
                                HashMap<String, String> facultyMap = new HashMap<String, String>();
                                facultyMap.put("name", facultyUser.getFullName());
                                facultyMap.put("status", statusList.get(facultyUser).toString());
                                if (user.getRole() == Role.FACULTY && facultyUser.equals(faculty)) {
                                    isMyTeam = true;
                                }
                                faculties.add(facultyMap);
                            }
                        }

                        //Adding all optional attendees
                        Set<String> optionaAttendees = b.getOptionalAttendees();
                        for (String optionalAttendee : optionaAttendees) {
                            HashMap<String, String> optionalMap = new HashMap<String, String>();
                            optionalMap.put("id", optionalAttendee);
                            optionalMap.put("name", optionalAttendee);
                            optionals.add(optionalMap);
                        }

                        //Setting the list of attendees
                        map.put("students", students);
                        map.put("faculties", faculties);
                        map.put("optionals", optionals);

                        //TODO: Things this code cannot get as of now (can only do this when database has values)
                        String TA = "-";
                        map.put("TA", TA);
                        String teamWiki = "-";
                        map.put("teamWiki", teamWiki);
                        if (user.getRole() == Role.FACULTY) {
                            map.put("isMyTeam", isMyTeam);
                        }
                    }

                    if (user.getRole() == Role.STUDENT) {
                        Team team = student.getTeam();
                        boolean available = true;
                        ArrayList<String> unavailable = new ArrayList<String>();

                        //TODO Update code after manage milestones is completed!
                        Milestone m = activeSchedule.getMilestone();
                        if (m.getName().equalsIgnoreCase("acceptance")) {
                            if (supervisorAvailability.contains(t)) { //Supervisor
                                available = false;
                                unavailable.add(team.getSupervisor().getFullName());
                            }
                        } else if (m.getName().equalsIgnoreCase("midterm")) {
                            if (reviewer1Availability.contains(t)) { //Reviewer 1
                                available = false;
                                unavailable.add(team.getReviewer1().getFullName());
                            }
                            if (reviewer2Availability.contains(t)) { //Reviewer 2
                                available = false;
                                unavailable.add(team.getReviewer2().getFullName());
                            }
                        } else if (m.getName().equalsIgnoreCase("final")) {
                            if (supervisorAvailability.contains(t)) { //Supervisor
                                available = false;
                                unavailable.add(team.getSupervisor().getFullName());
                            }
                            if (reviewer1Availability.contains(t)) { //Reviewer 1
                                available = false;
                                unavailable.add(team.getReviewer1().getFullName());
                            }
                        }

                        map.put("available", available);
                        map.put("unavailable", unavailable);
                    }

                    //Miscellaneous Role specific information
                    if (user.getRole() == Role.FACULTY) {
                        boolean available = true;
                        if (supervisorAvailability.contains(t)) {
                            available = false;
                        }
                        map.put("available", available);
                    } else if (user.getRole() == Role.TA && t.getTA() != null) {
                        map.put("taId", t.getTA().getId());
                    }
                    mapList.add(map);
                }
                logger.info("mapList size: " + mapList.size());
                json.put("timeslots", mapList);
            } else {
                //Return only milestones for this term
                List<Milestone> milestonesForTerm = MilestoneManager.findByTerm(em, term);
                ObjectMapper mapper = new ObjectMapper();
                String milestonesJson = mapper.writeValueAsString(milestonesForTerm);
                json.put("milestones", mapper.readValue(milestonesJson, List.class));
            }
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
