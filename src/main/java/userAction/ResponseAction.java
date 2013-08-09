/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.opensymphony.xwork2.ActionSupport;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import model.Schedule;
import model.Timeslot;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import constant.Status;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.servlet.RequestDispatcher;
import manager.RoleManager;
import manager.ScheduleManager;
import model.Role;
import model.Team;
import model.Term;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class ResponseAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(ResponseAction.class);
    private long termId;
    private long userId;
    private String teamName;
    private long teamId;
    private long timeslotId;
    private String milestoneName;
    private String startTime;
    private String endTime;
    private String venue;
    private String myStatus;
    private String userRole;
    private ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

    @Override
    public String execute() throws Exception {
        try {
            EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();

            HttpSession session = request.getSession();
            //Getting the id of user
            User user = (User) session.getAttribute("user");
            userId = user.getId();

            //Getting the user's roles for the active term
            List<Role> userAllRoles = new ArrayList<Role>();
            //Term activeTerm = MiscUtil.getActiveTerm(em);
			Term activeTerm = (Term) session.getAttribute("currentActiveTerm");
            List<Role> activeRoles = RoleManager.getAllRolesByTerm(em, activeTerm);
            for (Role role : activeRoles) {
                List<User> listUsers = role.getUsers();
                for (User userObj : listUsers) {
                    if (user.equals(userObj)) {
                        userAllRoles.add(role);
                    }
                }
            }

            //List<Role> userAllRoles = user.getRoles();   //This is to get all user roles (and not for active semester)
            List<Role> supervisorReviewerRoles = new ArrayList<Role>();
            for (Role role : userAllRoles) {
                if (role.getName().equalsIgnoreCase("Supervisor") || role.getName().equalsIgnoreCase("Reviewer")) {
                    supervisorReviewerRoles.add(role);
                }
            }

            //Checking whether the user is a supervisor/reviewer. Only supervisor/reviewer can approve or reject a booking
            if (supervisorReviewerRoles.size() > 0) {
                //Getting all schedules for the active term
                List<Schedule> listSchedules = ScheduleManager.findByTerm(em, activeTerm);
                List<Timeslot> userTimeslots = new ArrayList<Timeslot>();
                if (listSchedules != null && listSchedules.size() > 0) {
					for (Schedule schedule: listSchedules) {
						Set<Timeslot> allTimeslots = schedule.getTimeslots();
						//Getting the pending timeslots for the particular user
						if (allTimeslots != null && allTimeslots.size() > 0) {
							//Iterating over the timeslots. Matching the user.
							for (Timeslot currentTimeslot : allTimeslots) {
								HashMap<User, Status> statusList = currentTimeslot.getStatusList();
								Iterator iter = statusList.keySet().iterator();
								while (iter.hasNext()) {
									User userObj = (User) iter.next();
									if (userObj.equals(user)) {
										userTimeslots.add(currentTimeslot);
									}
								}
							}
						}
					} 

                    //Putting all the timeslot details for the user in hash map to display it 
                    //Sorting the timeslots list (Pending first, Approved/Rejected later)
                    ArrayList<HashMap<String, String>> pendingList = new ArrayList<HashMap<String, String>>();
                    ArrayList<HashMap<String, String>> approveRejectList = new ArrayList<HashMap<String, String>>();
                    if (userTimeslots.size() > 0) {
                        for (Timeslot timeslot : userTimeslots) {
                            HashMap<String, String> map = new HashMap<String, String>();
                            //SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm aa");
							SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy, EEE HH:mm aa");
                            venue = timeslot.getVenue();
                            timeslotId = timeslot.getId();
                            Team team = timeslot.getTeam();
                            teamId = team.getId();
                            teamName = team.getTeamName();
                            milestoneName = timeslot.getSchedule().getMilestone().getName();
                            startTime = sdf.format(timeslot.getStartTime());
                            endTime = sdf.format(timeslot.getEndTime());
                            myStatus = timeslot.getStatusList().get(user).toString();
                            //A user can only have 1 role in a team (Supervisor and Reviewer cannot be same for the same team)
                            if (team.getSupervisor().equals(user)) {
                                userRole = "Supervisor";
                            } else if (team.getReviewer1().equals(user)) {
                                userRole = "Reviewer";
                            } else if (team.getReviewer2().equals(user)) {
                                userRole = "Reviewer";
                            }

                            map.put("timeslotId", String.valueOf(timeslotId));
                            map.put("teamName", teamName);
                            map.put("milestone", milestoneName);
                            map.put("userRole", userRole);
                            map.put("startTime", startTime);
                            map.put("endTime", endTime);
                            map.put("venue", venue);
                            map.put("myStatus", myStatus);

                            if (map.get("myStatus").equalsIgnoreCase("Pending")) {
                                pendingList.add(map);
                            } else {
                                approveRejectList.add(map);
                            }
                        }

                        for (int i = 0; i < (pendingList.size() + approveRejectList.size()); i++) {
                            if (i < pendingList.size()) {
                                data.add(pendingList.get(i));
                            } else {
								int size = i - pendingList.size();
                                data.add(approveRejectList.get(size));
                            }
                        } //end of loop
                    }
                }
                return SUCCESS;
            }
            request.setAttribute("error", "Oops. You're not authorized to access this page!");
            logger.error("User cannot access this page");
            return ERROR;
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            request.setAttribute("error", "Error with Response: Escalate to developers!");
            return ERROR;
        }
    }

    public long getTermId() {
        return termId;
    }

    public void setTermId(long termId) {
        this.termId = termId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public String getMilestoneName() {
        return milestoneName;
    }

    public void setMilestoneName(String milestoneName) {
        this.milestoneName = milestoneName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public ArrayList<HashMap<String, String>> getData() {
        return data;
    }

    public void setData(ArrayList<HashMap<String, String>> data) {
        this.data = data;
    }

    public long getTimeslotId() {
        return timeslotId;
    }

    public void setTimeslotId(long timeslotId) {
        this.timeslotId = timeslotId;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getMyStatus() {
        return myStatus;
    }

    public void setMyStatus(String myStatus) {
        this.myStatus = myStatus;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
}