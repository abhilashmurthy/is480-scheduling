/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import model.Schedule;
import model.Timeslot;
import model.User;
import model.role.Faculty;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class SetAvailabilityAction extends ActionSupport implements ServletRequestAware{
	private static Logger logger = LoggerFactory.getLogger(SetAvailabilityAction.class);
	private HttpServletRequest request;
	private HashMap<String, Object> json = new HashMap<String, Object>();

	@Override
	public String execute() throws Exception {
		EntityManager em = null;
		try {
			em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
			User user = (User) request.getSession().getAttribute("user");
			if (user.getRole() != Role.FACULTY) {
				json.put("success", false);
				json.put("message", "Cannot set availability. User is not a faculty member.");
				return SUCCESS;
			}
			Faculty faculty = em.find(Faculty.class, user.getId());
			
			Map parameters = request.getParameterMap();

			//Getting timeslot values
			String[] timeslotIdArray = (String[]) parameters.get("timeslot_data[]");
			
			HashSet<Timeslot> availability = new HashSet<Timeslot>();
			//Populate timeslots in availability list
			if (timeslotIdArray != null && timeslotIdArray.length > 0) {
				for (String s : timeslotIdArray) {
					Long timeslotId = Long.parseLong(s.split("_")[1]);
					Timeslot t = em.find(Timeslot.class, timeslotId);
					availability.add(t);
				}
			}
			
			em.getTransaction().begin();
			faculty.setUnavailableTimeslots(availability);
			em.persist(faculty);
			em.getTransaction().commit();
			
			//Reloading the user object in the session
			request.getSession().setAttribute("user", faculty);
			
			json.put("success", true);
            json.put("message", "Faculty availability updated successfully");
		} catch (Exception e) {
			if (em != null && em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			
			logger.error(e.getMessage());
			if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
					if (s.getClassName().startsWith("SetAvailabilityAction")) {
						break;
					}
                }
            }
			
			json.put("success", false);
            json.put("message", "Error with SetAvailability: Escalate to developers!");
		} finally { //Closing the EntityManager to prevent memory leak
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
		return SUCCESS;
	}

	public HashMap<String, Object> getJson() {
		return json;
	}

	public void setJson(HashMap<String, Object> json) {
		this.json = json;
	}
	
	public void setServletRequest(HttpServletRequest hsr) {
		request = hsr;
	}
	
}