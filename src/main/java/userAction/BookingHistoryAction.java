/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
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
import manager.RoleManager;
import model.Role;
import model.Team;
import model.Term;
import static userAction.ResponseAction.logger;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class BookingHistoryAction {//extends ActionSupport implements ServletRequestAware{
//	private long termId;
//    private long userId;
//	private String teamName;
//	private long teamId;
//	private long timeslotId;
//	private String milestoneName;
//	private String startTime;
//	private String endTime;
//	private String venue;
//	private String myStatus;
//	private String userRole;
//	private ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
//	private HttpServletRequest request;    
//    static final Logger logger = LoggerFactory.getLogger(ResponseAction.class);
//
//	@Override
//    public String execute() throws Exception {
//		EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
//		
//		HttpSession session = request.getSession();
//		//Getting the id of user
//		User user = (User) session.getAttribute("user");
//		userId = user.getId();
//		
//		//Getting the current term 
////		Term term = MiscUtil.getActiveTerm();
////		termId = term.getId();
//		
//		//Getting the user's roles for the active term
//		List<Role> userAllRoles = new ArrayList<Role>();
//		Term activeTerm = MiscUtil.getActiveTerm(em);
//		List<Role> activeRoles = RoleManager.getAllRolesByTerm(em, activeTerm);
//		for (Role role: activeRoles) {
//			List<User> listUsers = role.getUsers();
//			for (User userObj: listUsers) {
//				if (user.equals(userObj)) {
//					userAllRoles.add(role);
//				}
//			}
//		}
//				
//		//List<Role> userAllRoles = user.getRoles();   //This is to get all user roles (and not for active semester)
//		List<Role> supervisorReviewerRoles = new ArrayList<Role>();
//		for (Role role: userAllRoles) {
//			if (role.getName().equalsIgnoreCase("Supervisor") || role.getName().equalsIgnoreCase("Reviewer")) {
//				supervisorReviewerRoles.add(role);
//			}
//		}
//		
//		//Checking whether the user is a supervisor/reviewer. Only supervisor/reviewer can approve or reject a booking
//		if (supervisorReviewerRoles.size() > 0) {
//			//Getting the current schedule based on term id
//			Schedule schedule = MiscUtil.getActiveSchedule(em);
//			//Getting the current milestone
//			//Set<Timeslot> pendingList = null;
//			List<Timeslot> userTimeslots = new ArrayList<Timeslot>();
//			if (schedule != null) {
//				Set<Timeslot> allTimeslots = schedule.getTimeslots();
//				//Getting the pending timeslots for the particular user
//				if (allTimeslots != null && allTimeslots.size() > 0) {
//					//Iterating over the timeslots. Matching the user and checking if the status is pending.
//					for(Timeslot currentTimeslot: allTimeslots) {
//						HashMap<User,Status> statusList = currentTimeslot.getStatusList();
//						Iterator iter = statusList.keySet().iterator();
//						while (iter.hasNext()) {
//							User userObj = (User) iter.next();
//							if (userObj.equals(user)) {
//								userTimeslots.add(currentTimeslot);
//							}
//						}
//					}
//				}
//				
//				//Putting all the timeslot details for the user in hash map to display it 
//				//Sorting the timeslots list (Pending first, Approved/Rejected later)
//				ArrayList<HashMap<String, String>> pendingList = new ArrayList<HashMap<String, String>>();
//				ArrayList<HashMap<String, String>> approveRejectList = new ArrayList<HashMap<String, String>>();
//				if (userTimeslots.size() > 0) {
//					for (Timeslot timeslot: userTimeslots) {
//						HashMap<String, String> map = new HashMap<String, String>();
//						SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm aa");
//						venue = timeslot.getVenue();
//						timeslotId = timeslot.getId();
//						Team team = timeslot.getTeam();
//						teamId = team.getId();				
//						teamName = team.getTeamName();
//						milestoneName = schedule.getMilestone().getName();
//						startTime = sdf.format(timeslot.getStartTime());
//						endTime = sdf.format(timeslot.getEndTime());
//						myStatus = timeslot.getStatusList().get(user).toString();
//						//A user can only have 1 role in a team (Supervisor and Reviewer cannot be same for the same team)
//						if (team.getSupervisor().equals(user)) {
//							userRole = "Supervisor";
//						} else if (team.getReviewer1().equals(user)) {
//							userRole = "Reviewer";
//						} else if (team.getReviewer2().equals(user)) {
//							userRole = "Reviewer";
//						}
//						
//						map.put("timeslotId", String.valueOf(timeslotId));
//						map.put("teamName", teamName);
//						map.put("milestone", milestoneName);
//						map.put("userRole", userRole);
//						map.put("startTime", startTime);
//						map.put("endTime", endTime);
//						map.put("venue", venue);
//						map.put("myStatus", myStatus);
//						
//						if (map.get("myStatus").equalsIgnoreCase("Pending")) {
//							pendingList.add(map);
//						} else {
//							approveRejectList.add(map);
//						}
//					}
//					
//					for (int i = 0; i < (pendingList.size() + approveRejectList.size()); i++) {
//						if (i < pendingList.size()) {
//							data.add(pendingList.get(i));
//						} else {
//							data.add(approveRejectList.get(i));
//						}
//					} //end of loop
//				}
//			}
////			String value = request.getParameter("value");
////			if (value.equals("1")) {
////				
////			}
//			return SUCCESS;
//		}
//        request.setAttribute("error", "Oops. You're not authorized to access this page!");
//		logger.error("User cannot access this page");
//		return ERROR;
//    }
	
}
