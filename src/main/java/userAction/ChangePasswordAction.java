/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opensymphony.xwork2.ActionSupport;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.SettingsManager;
import model.Settings;
import model.SystemActivityLog;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.CustomException;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class ChangePasswordAction extends ActionSupport implements ServletRequestAware {
	
	private HttpServletRequest request;
	private Logger logger = LoggerFactory.getLogger(ChangePasswordAction.class);
	private HashMap<String, Object> json = new HashMap<String, Object>();

	@Override
	public String execute() throws Exception {
		HttpSession session = request.getSession();
		User user = (User)session.getAttribute("user");
		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());
		
		SystemActivityLog logItem = new SystemActivityLog();
		logItem.setActivity("Administrator: Change Administrator Password");
		logItem.setRunTime(now);
		if (user.getId() != null) logItem.setUser(user);
		logItem.setMessage("Error with validation / No changes made");
		logItem.setSuccess(true);
		
		EntityManager em = null;
        try {
			em = MiscUtil.getEntityManagerInstance();
			
			JsonObject inputData = new Gson().fromJson(request.getParameter("jsonData"), JsonObject.class);
			
			JsonElement currPassElem = inputData.get("currentPassword");
			if (currPassElem == null) throw new CustomException("Please specify the current password");
			String currentPassword = currPassElem.getAsString();
			
			JsonElement newPassElem = inputData.get("newPassword");
			if (newPassElem == null) throw new CustomException("Please specify the new password");
			String newPassword = newPassElem.getAsString();
			
			JsonElement verifyPassElem = inputData.get("verifyPassword");
			if (verifyPassElem == null) throw new CustomException("Please specify the verified new password");
			String verifyPassword = verifyPassElem.getAsString();
			
			//Checking if the new password matches the confirmation/verification
			if (!newPassword.equals(verifyPassword)) throw new CustomException("New password doesn't match the confirmation. Please try again!");
			
			//Checking if the new password is blank
			if (newPassword.trim().equals("")) throw new CustomException("New password cannot be blank. Please try again!");
			
			em.getTransaction().begin();
			
			Settings bypassPassword = SettingsManager.getByName(em, "bypassPassword");
			if (!bypassPassword.getValue().equals(currentPassword)) throw new CustomException("Current password does not match. Please try again!");
			
			bypassPassword.setValue(newPassword.trim());
			
			em.getTransaction().commit();
			
			json.put("success", true);
			logItem.setMessage("Password changed successfully");
			
        } catch (CustomException e) {
			json.put("success", false);
            json.put("message", e.getMessage());
		} catch (Exception e) {
			logItem.setSuccess(false);
			if (user.getId() != null) logItem.setUser(user);
			logItem.setMessage("Error: " + e.getMessage());
			
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
			json.put("success", false);
            json.put("exception", true);
            json.put("message", "Error with ChangePasswordAction: Escalate to developers!");
        } finally {
			 if (em != null) {
				if (em.getTransaction().isActive()) em.getTransaction().rollback();
                //Saving job log in database
                if (!em.getTransaction().isActive()) em.getTransaction().begin();
                em.persist(logItem);
                em.getTransaction().commit();
                if (em.isOpen()) em.close();
			}
		}
		
		return SUCCESS;
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

}
