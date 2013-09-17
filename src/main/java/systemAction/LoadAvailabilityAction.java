/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import static com.opensymphony.xwork2.Action.ERROR;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.util.ArrayList;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import model.Timeslot;
import model.User;
import model.role.Faculty;
import org.apache.struts2.interceptor.ServletRequestAware;
import static systemAction.ManageActiveTermsAction.logger;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class LoadAvailabilityAction extends ActionSupport implements ServletRequestAware{
	private HttpServletRequest request;
	
	private ArrayList<String> unavailableTimeslotIds = new ArrayList<String>();

	@Override
	public String execute() throws Exception {
		EntityManager em = null;
		try {
			em = MiscUtil.getEntityManagerInstance();
			
			//Checking whether the active user is a faculty member
			User user = (User) request.getSession().getAttribute("user");
			if (user.getRole() != Role.FACULTY) {
				request.setAttribute("error", "Oops. You're not authorized to access this page!");
				logger.error("User cannot access this page");
				return ERROR;
			}
			
			Faculty f = em.find(Faculty.class, user.getId());
			Set<Timeslot> slots = f.getUnavailableTimeslots();
			
			for (Timeslot t : slots) {
				unavailableTimeslotIds.add(t.getId().toString());
			}
			
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

	public ArrayList<String> getUnavailableTimeslotIds() {
		return unavailableTimeslotIds;
	}

	public void setUnavailableTimeslotIds(ArrayList<String> unavailableTimeslotIds) {
		this.unavailableTimeslotIds = unavailableTimeslotIds;
	}

	public void setServletRequest(HttpServletRequest hsr) {
		request = hsr;
	}
	
}
