/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
//import constant.Status;
import java.io.IOException;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.SettingsManager;
import model.Settings;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONArray;
import org.json.JSONObject;
import util.MiscUtil;

/**
 *
 * @author Tarlochan
 */
public class UpdateNotificationSettingsAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    private String settingDetails;
    private HashMap<String, Object> json = new HashMap<String, Object>();

    @Override
    public String execute() throws ServletException, IOException {
		EntityManager em = null;
        try {
            json.put("exception", false);
            em = MiscUtil.getEntityManagerInstance();
            HttpSession session = request.getSession();
            
            User user = (User) session.getAttribute("user");
			
			if (user.getRole().equals(Role.ADMINISTRATOR) || user.getRole().equals(Role.COURSE_COORDINATOR)) {

				//get the current milestone settings
				Settings currentSettings = SettingsManager.getByName(em, "manageNotifications");

				//convert settingsDetails into an array
				String[] setArr = settingDetails.split(",");

				JSONArray notificationArray = new JSONArray(currentSettings.getValue());

				JSONObject email = notificationArray.getJSONObject(0);
				JSONObject sms = notificationArray.getJSONObject(1);


				email.put("emailStatus",setArr[1]);
				email.put("emailFrequency",setArr[2]);


				sms.put("smsStatus",setArr[4]);
				sms.put("smsFrequency",setArr[5]);

				try{	
					//set to the updated list                				
					em.getTransaction().begin();
					currentSettings.setValue(notificationArray.toString());
					em.persist(currentSettings);
					em.getTransaction().commit();

					//if the settings have been saved

					json.put("success",true);
					json.put("message", "Settings saved successfully!");

				} catch (Exception e) {   
					if (MiscUtil.DEV_MODE) {
						for (StackTraceElement s : e.getStackTrace()) {
						}
					}
					json.put("success", false);
					json.put("exception", true);
					json.put("message", "Error with persisting settings object: Escalate to developers!");
				}
			}else {
				//Incorrect user role
				json.put("error", true);
				json.put("message", "You are not authorized to access this page!");
			}

        } catch (Exception e) {
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                }
            }
            json.put("success", false);
            json.put("exception", true);
            json.put("message", "Error with changing notification settings");
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}
        return SUCCESS;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }

    public String getSettingDetails() {
        return settingDetails;
    }

    public void setSettingDetails(String settingDetails) {
        this.settingDetails = settingDetails;
    }

    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }
}
