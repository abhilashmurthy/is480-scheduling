/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import manager.UserManager;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.CustomException;
import util.MiscUtil;

/**
 * JSON package action
 */
public class ManageUserAction extends ActionSupport implements ServletRequestAware {
	
	private HttpServletRequest request;
	private HashMap<String, Object> json = new HashMap<String, Object>();
	private Logger logger = LoggerFactory.getLogger(ManageUserAction.class);
	private EntityManager em = null;

	@Override
	public String execute() throws Exception {
		try {
			em = MiscUtil.getEntityManagerInstance();
			User user = (User) request.getSession().getAttribute("user");
			
			//Checking if the user is allowed to perform this function
			if (user.getRole() != Role.ADMINISTRATOR && user.getRole() != Role.COURSE_COORDINATOR) {
				json.put("success", false);
				json.put("message", "User does not have access to this page!");
				return SUCCESS;
			}
			
			//Retrieveing the form data
			JsonObject dataObj = new Gson().fromJson(request.getParameter("jsonData"), JsonObject.class);
			String actionType = dataObj.get("action").getAsString(); //Getting the action type being performed
			
			//Beginning transaction
			em.getTransaction().begin();
			
			boolean result;
			if (actionType.equalsIgnoreCase("add") || actionType.equalsIgnoreCase("edit")) { //Add or edit a user
				result = addEditUser(actionType, dataObj);
			} else if (actionType.equalsIgnoreCase("delete")) { //Delete an existing user
				result = deleteUser(dataObj);
			} else {
				json.put("success", false);
				json.put("message", "Unknown action. Options: add/edit/delete");
				return SUCCESS;
			}
			
			//Committing transaction if result = true
			if (result) em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
					if (s.getClassName().equals(this.getClass().getName())) {
						logger.error(s.toString());
					}
                }
            }
            json.put("success", false);
            json.put("exception", true);
            json.put("message", "Error with ManageUser: Escalate to developers!");
        } finally {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            if (em != null && em.isOpen()) em.close();
        }
        return SUCCESS;
	}
	
	//Method to add a new user to the system
	public boolean addEditUser(String actionType, JsonObject dataObj) {
		try {
			//Getting the role of the user object to be created
			JsonElement roleInfo = dataObj.get("type");
			if (roleInfo == null) throw new CustomException("Please specify the role!");
			Role role;
			try {
				role = Role.valueOf(roleInfo.getAsString());
			} catch (IllegalArgumentException e) {
				throw new CustomException("Specified role not found");
			}
			
			long termId = 0;
			JsonElement termIdInfo = dataObj.get("termId");
			if (termIdInfo != null) termId = termIdInfo.getAsLong();
			
			String username = dataObj.get("username").getAsString();
			String fullName = dataObj.get("fullName").getAsString();
			
			long teamId = 0;
			JsonElement teamIdInfo = dataObj.get("teamId");
			if (teamIdInfo != null && teamIdInfo.getAsLong() != -1L) teamId = teamIdInfo.getAsLong();
			
			long existingUserId = 0;
			if (actionType.equalsIgnoreCase("edit")) {
				JsonElement userIdInfo = dataObj.get("userId");
				if (userIdInfo == null) throw new CustomException("Please specify the user ID for editing!");
				else existingUserId = userIdInfo.getAsLong();
			}
			
			json = UserManager.addEditUser(em, role, termId, username, fullName, teamId, existingUserId);
			return true;
		} catch (CustomException e) {
			json.put("success", false);
			json.put("message", e.getMessage());
			return false;
		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage());
			for (StackTraceElement s : e.getStackTrace()) {
				if (s.getClassName().equals(this.getClass().getName())) {
					logger.error(s.toString());
				}
			}
			json.put("success", false);
			json.put("message", "Oops. Something went wrong. Please try again!");
			return false;
		}
	}
	
	//Method to delete an existing user from the system
	public boolean deleteUser(JsonObject dataObj) {
		try {
			long deleteUserId = 0;
			JsonElement userIdInfo = dataObj.get("userId");
			if (userIdInfo != null) deleteUserId = userIdInfo.getAsLong();
			UserManager.deleteUser(em, deleteUserId);
			
			json.put("success", true);
			return true;
		} catch (CustomException e) {
			json.put("success", false);
			json.put("message", e.getMessage());
			return false;
		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage());
			for (StackTraceElement s : e.getStackTrace()) {
				logger.error(s.toString());
			}
			json.put("success", false);
			json.put("message", "Oops. Something went wrong. Please try again!");
			return false;
		}
		
	}

	public HashMap<String, Object> getJson() {
		return json;
	}

	public void setJson(HashMap<String, Object> json) {
		this.json = json;
	}

	public void setServletRequest(HttpServletRequest hsr) {
		this.request = hsr;
	}
}
