/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import com.google.gson.Gson;
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
    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(GetMilestoneSettingsAction.class);
	private String remindersJson;
	
	@Override
    public String execute() {
		EntityManager em = null;
        try {
            em = MiscUtil.getEntityManagerInstance();
			HttpSession session = request.getSession();
			
			//Checking whether the active user is admin/coursee coordinator or not
			User user = (User) session.getAttribute("user");
			if (user.getRole().equals(Role.ADMINISTRATOR) || user.getRole().equals(Role.COURSE_COORDINATOR)) {
				remindersJson = SettingsManager.getNotificationSettings(em).getValue();
			}
		} catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
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
	
	public String getRemindersJson() {
		return remindersJson;
	}

	public void setRemindersJson(String remindersJson) {
		this.remindersJson = remindersJson;
	}

}  // end of class


