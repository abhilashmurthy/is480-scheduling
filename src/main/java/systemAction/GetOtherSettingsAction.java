/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.util.ArrayList;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.SettingsManager;
import model.Settings;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Tarlochan
 */
public class GetOtherSettingsAction extends ActionSupport implements ServletRequestAware {
	
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

				//Getting the email settings value
				Settings notificationSettings = SettingsManager.getNotificationSettings(em);
				
				JSONArray notificationArray = new JSONArray(notificationSettings.getValue());
				//String[] setArr = settingsList.split(",");
				
				HashMap<String, Object> map = new HashMap<String, Object>();
				
				//loop through the milestonArray
				for (int i = 0; i < notificationArray.length(); i++) {
					
					JSONObject obj = notificationArray.getJSONObject(i);
					
					if(i==0){
						String getEmailStatus = obj.getString("emailStatus");
						int emailFrequency = obj.getInt("emailFrequency");
						
						map.put("emailStatus",getEmailStatus);
						map.put("emailFrequency",emailFrequency);
						
						
					}else if(i==1){
						String smsEmailStatus = obj.getString("smsStatus");
						int smsFrequency = obj.getInt("smsFrequency");
						
						map.put("smsStatus",smsEmailStatus);
						map.put("smsFrequency",smsFrequency);
						
					}else if(i==2){
						String smsEmailStatus = obj.getString("emailClearStatus");
						int smsFrequency = obj.getInt("emailClearFrequency");
						
						map.put("emailClearStatus",smsEmailStatus);
						map.put("emailClearFrequency",smsFrequency);

					}

				}
				
				

				/*map.put("emailStatus",setArr[1]);
				map.put("emailFrequency",setArr[2]);
				map.put("smsStatus",setArr[4]);
				map.put("smsFrequency",setArr[5]);*/
				data.add(map);
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


