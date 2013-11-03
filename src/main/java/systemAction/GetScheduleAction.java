/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.BookingStatus;
import constant.Response;
import constant.Role;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
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
import model.role.TA;
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
            em = MiscUtil.getEntityManagerInstance();

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat viewDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
            SimpleDateFormat viewTimeFormat = new SimpleDateFormat("HH:mm");

            HttpSession session = request.getSession();
            Term term = (Term) session.getAttribute("currentActiveTerm");

            if (milestone != null && !milestone.equals("")) {
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

				//Populating specific user role obejcts and getting unavailable timeslots if user is a faculty member
                User user = (User) request.getSession().getAttribute("user");
				Set<Timeslot> facultyAvailability = null;
				if (user.getRole() == Role.STUDENT) {
					student = em.find(Student.class, user.getId());
				} else if (user.getRole() == Role.FACULTY) {
                    faculty = em.find(Faculty.class, user.getId());
                    facultyAvailability = faculty.getUnavailableTimeslots();
                }

                ArrayList<HashMap<String, Object>> mapList = new ArrayList<HashMap<String, Object>>();
                for (Timeslot t : activeSchedule.getTimeslots()) {

                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("id", t.getId());
                    map.put("datetime", dateFormat.format(t.getStartTime()) + " " + timeFormat.format(t.getStartTime()));

                    //Getting venue for timeslot
                    String venue = t.getVenue();
                    map.put("venue", venue);

                    if (t.getCurrentBooking() != null) {
                        Booking b = t.getCurrentBooking();
                        boolean isMyTeam = false; //Variable to track if the booking belongs to the faculty member logged in
                        Date startDate = new Date(t.getStartTime().getTime());
                        Date endDate = new Date(t.getEndTime().getTime());
						map.put("bookingId", b.getId());
                        map.put("team", b.getTeam().getTeamName());
						map.put("wiki", b.getTeam().getWiki());

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
                                    isMyTeam = true; //Booking belongs to faculty member as he's part of the response list
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
						
						//Adding the subscribe users
						Set<String> subscribedUsers = new HashSet<String>();
						for (User s : b.getSubscribedUsers()) {
							subscribedUsers.add(s.getUsername() + "@smu.edu.sg");
						}

                        //Setting the list of attendees
                        map.put("students", students);
                        map.put("faculties", faculties);
                        map.put("optionals", optionals);
						map.put("subscribedUsers", subscribedUsers);
                        if (user.getRole() == Role.FACULTY) {
                            map.put("isMyTeam", isMyTeam);
                        }
                    }
                    
                    TA ta = t.getTA();
                    if(ta!=null){
                        map.put("TA", ta.getFullName());
                    }else{
                        map.put("TA", "-");
                    }
                    

                    if (user.getRole() == Role.STUDENT) {
                        Team team = student.getTeam();
						boolean available = true;
						ArrayList<String> unavailable = new ArrayList<String>();
						if (team != null) {							
							Milestone m = activeSchedule.getMilestone();
							ArrayList<String> requiredAttendees = m.getRequiredAttendees();
							for (String roleName : requiredAttendees) {
								Method roleGetter = Team.class.getDeclaredMethod("get" + roleName, null);
								Faculty roleUser = (Faculty) roleGetter.invoke(team, null);
								if (roleUser.getUnavailableTimeslots().contains(t)) {
									available = false;
									unavailable.add(roleUser.getFullName());
								}
							}

							//Get latest previous booking for the current schedule
							Query bookingsQuery = em.createQuery("select b from Booking b where b.timeslot = :timeslotId and b.team = :teamId and b.lastEditedAt = "
														+ "(select MAX(c.lastEditedAt) from Booking c where c.team = :teamId and (c.bookingStatus = :deletedBookingStatus or c.bookingStatus = :rejectedBookingStatus) and c.timeslot.schedule = :scheduleId)")
														.setParameter("timeslotId", t)
														.setParameter("teamId", team)
														.setParameter("deletedBookingStatus", BookingStatus.DELETED)
														.setParameter("rejectedBookingStatus", BookingStatus.REJECTED)
														.setParameter("scheduleId", t.getSchedule())
														.setMaxResults(1);
							try {
								Booking lastBooking = (Booking) bookingsQuery.getSingleResult();
								//Add only the single last booking
								map.put("lastBookingWasRemoved", true);
								map.put("lastBookingEditedBy", lastBooking.getLastEditedBy());
								map.put("lastBookingRejectReason", lastBooking.getRejectReason());
							} catch (NoResultException n) {
								//This is normal, there was no old booking found
							}
						}
						map.put("available", available);
						map.put("unavailable", unavailable);
                    }

                    //Miscellaneous Role specific information
                    if (user.getRole() == Role.FACULTY) {
                        boolean available = true;
                        if (facultyAvailability.contains(t)) {
                            available = false;
                        }
                        map.put("available", available);
                    } else if (t.getTA() != null) {
                        map.put("taId", t.getTA().getId());
                    }
                    
                    mapList.add(map);
                }
                json.put("timeslots", mapList);
            } else {
                //Return only milestones for this term
				List<Milestone> milestonesForTerm = MilestoneManager.findByTerm(em, term);
				ArrayList<HashMap<String, Object>> mapList = new ArrayList<HashMap<String, Object>>();
				for (Milestone m : milestonesForTerm) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					Schedule s = ScheduleManager.findByMilestone(em, m);
					map.put("id", m.getId());
					map.put("scheduleId", s.getId());
					map.put("milestoneOrder", m.getMilestoneOrder());
					map.put("name", m.getName());
					map.put("slotDuration", m.getSlotDuration());
					map.put("bookable", s.isBookable());
					mapList.add(map);
				}
                json.put("milestones", mapList);
            }
            json.put("success", true);
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getClass().getName() + " " + e.getMessage());
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
