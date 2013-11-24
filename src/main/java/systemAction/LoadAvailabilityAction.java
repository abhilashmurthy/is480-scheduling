/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import com.google.gson.Gson;
import static com.opensymphony.xwork2.Action.ERROR;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.UserManager;
import model.Term;
import model.Timeslot;
import model.User;
import model.role.Faculty;
import org.apache.struts2.interceptor.ServletRequestAware;
import static systemAction.PrepareActiveTermsAction.logger;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class LoadAvailabilityAction extends ActionSupport implements ServletRequestAware{
	private HttpServletRequest request;
	private String myUnavailableJson;
	private String othersUnavailableJson;

	@Override
	public String execute() throws Exception {
		EntityManager em = null;
		HttpSession session = request.getSession();
		try {
			em = MiscUtil.getEntityManagerInstance();
			em.getTransaction().begin();
			Gson gson = new Gson();
			Term term = (Term) session.getAttribute("currentActiveTerm");
			
			//Checking whether the active user is a faculty member
			User user = (User) request.getSession().getAttribute("user");
			if (user.getRole() != Role.FACULTY) {
				request.setAttribute("error", "Oops. You're not authorized to access this page!");
				logger.error("User cannot access this page");
				return ERROR;
			}
			
			Faculty me = em.find(Faculty.class, user.getId());
			Set<Timeslot> slots = me.getUnavailableTimeslots();
			
			List<String> unavailableTimeslots = new ArrayList<String>();
			for (Timeslot t : slots) {
				unavailableTimeslots.add("timeslot_" + t.getId());
			}
			myUnavailableJson = gson.toJson(unavailableTimeslots);
			
			List<HashMap<String, Object>> otherFacultyData = new ArrayList<HashMap<String, Object>>();
			List<User> users = UserManager.getUsersByRoleAndTerm(em, Role.FACULTY, term);
			for (User currentUser : users) {
				Faculty faculty = (Faculty) currentUser;
				if (faculty.equals(me)) continue;
				HashMap<String, Object> facultyMap = new HashMap<String, Object>();
				facultyMap.put("fullName", faculty.getFullName());
				facultyMap.put("username", faculty.getUsername());
				facultyMap.put("id", faculty.getId());
				List<String> otherUnavailableTimeslots = new ArrayList<String>();
				for (Timeslot t : faculty.getUnavailableTimeslots()) {
					otherUnavailableTimeslots.add("timeslot_" + t.getId());
				}
				facultyMap.put("unavailableTimeslots", otherUnavailableTimeslots);
				otherFacultyData.add(facultyMap);
			}
			othersUnavailableJson = gson.toJson(otherFacultyData);
			
			em.getTransaction().commit();
			return SUCCESS;
		} catch (Exception e) {
			 logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            request.setAttribute("error", "Error with LoadAvailability: Escalate to developers!");
            return ERROR;
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}
	}
	
	public String getMyUnavailableJson() {
		return myUnavailableJson;
	}

	public void setMyUnavailableJson(String myUnavailableJson) {
		this.myUnavailableJson = myUnavailableJson;
	}

	public String getOthersUnavailableJson() {
		return othersUnavailableJson;
	}

	public void setOthersUnavailableJson(String othersUnavailableJson) {
		this.othersUnavailableJson = othersUnavailableJson;
	}

	public void setServletRequest(HttpServletRequest hsr) {
		request = hsr;
	}
	
}
