/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import com.google.gson.Gson;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.SettingsManager;
import model.Settings;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class GetMilestoneSettingsAction extends ActionSupport implements ServletRequestAware {
	
	private HashMap<String, Object> json = new HashMap<String, Object>();
	private ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(GetMilestoneSettingsAction.class);
	
	@Override
    public String execute() {
		EntityManager em = null;
        try {
            em = MiscUtil.getEntityManagerInstance();
			HttpSession session = request.getSession();
			
			//Checking whether the active user is admin/coursee coordinator or not
			User user = (User) session.getAttribute("user");
			if (user.getRole().equals(Role.ADMINISTRATOR) || user.getRole().equals(Role.COURSE_COORDINATOR)) {
				//Getting the current settings object for milestones
				Settings result = SettingsManager.getByName(em, "milestones");
				//Getting the milestones value from settings object
				ArrayList<HashMap<String,Object>> settingsList = SettingsManager.getMilestoneSettings(em, result);
				for (HashMap<String, Object> map : settingsList) {
					double duration = (Double) map.get("duration");
					int dur = (int) duration;
					map.put("duration", String.valueOf(dur));

					double order = (Double) map.get("order");
					int o = (int) order;
					map.put("order", String.valueOf(o));

					String milestone = (String) map.get("milestone");
					map.put("milestone", milestone);
					map.put("id", milestone.replaceAll(" ", ""));

					ArrayList<String> attendees = (ArrayList<String>) map.get("attendees");
					List<HashMap<String, String>> attendeesList = new ArrayList<HashMap<String, String>>();
					if (attendees.size() > 0) {
						for (String attendee: attendees) {
							HashMap<String, String> userMap = new HashMap<String, String>();
							userMap.put("attendee", attendee);
							attendeesList.add(userMap);
						}
					}
					map.put("attendees", attendeesList);
					map.put("attendeesJson", new Gson().toJson(attendees));

					data.add(map);
				}
			} else {
				//Incorrect user role
				json.put("error", true);
				json.put("message", "You are not authorized to access this page!");
			}
		} catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("message", "Error with GetMilestoneSettings: Escalate to developers!");
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
}  // end of class
