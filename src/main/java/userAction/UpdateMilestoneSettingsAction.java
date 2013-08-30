/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.google.gson.Gson;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import manager.SettingsManager;
import static manager.SettingsManager.getByName;
import model.Settings;
import model.User;
import systemAction.GetMilestoneSettingsAction;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class UpdateMilestoneSettingsAction extends ActionSupport implements ServletRequestAware {

    private HashMap<String, Object> json = new HashMap<String, Object>();
	private ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
	String pastOrderNumber;
	String newOrderNumber;
	String newMilestoneName;
	String newDuration;
	String newAttendees;
    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(UpdateMilestoneSettingsAction.class);
	
	@Override
    public String execute() {
		EntityManager em = null;
        try {
            em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
			HttpSession session = request.getSession();
			
			//Checking whether the active user is admin/course coordinator or not
			User user = (User) session.getAttribute("user");
			if (user.getRole().equals(Role.ADMINISTRATOR) || user.getRole().equals(Role.COURSE_COORDINATOR)) {
				//Getting the current settings object for milestones
				Settings currentSettings = SettingsManager.getByName(em, "milestones");
				//Getting the milestones value from settings object
				ArrayList<HashMap<String,Object>> settingsList = SettingsManager.getMilestoneSettings(em, currentSettings);
				//New settings for milestone
				ArrayList<HashMap<String,Object>> updatedSettingsList = new ArrayList<HashMap<String, Object>>();
			
				for (HashMap<String, Object> mapToUpdate : settingsList) {
					double order = (Double) mapToUpdate.get("order");
					//Updating the milestone based on the past order number
					if (order == Double.valueOf(pastOrderNumber)) {
						mapToUpdate.put("order", Double.valueOf(newOrderNumber));
						mapToUpdate.put("name", newMilestoneName);
						mapToUpdate.put("duration", Double.valueOf(newDuration));
						//To remove the comma from the end (CHANGE LATER)
						newAttendees = newAttendees.substring(0, newAttendees.length() - 1);
						String[] requiredAttendees = newAttendees.split(",");
						mapToUpdate.put("requiredAttendees", requiredAttendees);
					}
					updatedSettingsList.add(mapToUpdate);
				}
				//String jsonString = new Gson().toJson(updatedSettingsList);
				em.getTransaction().begin();
				currentSettings.setValue(new Gson().toJson(updatedSettingsList));
				em.persist(currentSettings);
				em.getTransaction().commit();
				json.put("success", true);
				json.put("message", "Milestone Settings Updated!");

			} else {
				//Incorrect user role
				json.put("error", true);
				json.put("message", "You are not authorized to access this functionality!");
			}
		} catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("message", "Error with UpdateMilestoneSettings: Escalate to developers!");
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}
        return SUCCESS;
	 } //end of execute
	 
	public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
	
	public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }

	public HashMap<String, Object> getJson() {
		return json;
	}

	public void setJson(HashMap<String, Object> json) {
		this.json = json;
	}

	public ArrayList<HashMap<String, Object>> getData() {
		return data;
	}

	public void setData(ArrayList<HashMap<String, Object>> data) {
		this.data = data;
	}

	public String getPastOrderNumber() {
		return pastOrderNumber;
	}

	public void setPastOrderNumber(String pastOrderNumber) {
		this.pastOrderNumber = pastOrderNumber;
	}

	public String getNewOrderNumber() {
		return newOrderNumber;
	}

	public void setNewOrderNumber(String newOrderNumber) {
		this.newOrderNumber = newOrderNumber;
	}

	public String getNewMilestoneName() {
		return newMilestoneName;
	}

	public void setNewMilestoneName(String newMilestoneName) {
		this.newMilestoneName = newMilestoneName;
	}

	public String getNewDuration() {
		return newDuration;
	}

	public void setNewDuration(String newDuration) {
		this.newDuration = newDuration;
	}

	public String getNewAttendees() {
		return newAttendees;
	}

	public void setNewAttendees(String newAttendees) {
		this.newAttendees = newAttendees;
	}
}  // end of class
