/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import manager.UserManager;
import model.Term;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static userAction.UpdateScheduleAction.logger;
import util.MiscUtil;

/**
 * JSON package action
 */
public class ManageUsersAction extends ActionSupport implements ServletRequestAware {
	
	private HttpServletRequest request;
	private HashMap<String, Object> json;
	private Logger logger = LoggerFactory.getLogger(ManageUsersAction.class);
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
			
			//God I wish they had switch-case for Strings (Java 7!)
			if (actionType.equalsIgnoreCase("add")) { //Add a new user
				addUser(dataObj);
			} else if (actionType.equalsIgnoreCase("edit")) { //Edit an existing user
				editUser();
			} else if (actionType.equalsIgnoreCase("delete")) { //Delete an existing user
				deleteUser();
			} else {
				json.put("success", false);
				json.put("message", "Unknown action. Options: add/edit/delete");
				return SUCCESS;
			}
			
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("exception", true);
            json.put("message", "Error with ManageUsers: Escalate to developers!");
        } finally {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            if (em != null && em.isOpen()) em.close();
        }
        return SUCCESS;
	}
	
	//Method to add a new user to the system
	public void	addUser(JsonObject dataObj) {
		try {
			//Getting the role of the user object to be created
			Role role = Role.valueOf(dataObj.get("type").getAsString());
			
			//Getting the term information if required
			Term term = null;
			//Term info not required for permanent roles: Administrator and Course Coordinator
			if (role != Role.ADMINISTRATOR && role != Role.COURSE_COORDINATOR) {
				long termId = dataObj.get("termId").getAsLong();
				term = em.find(Term.class, termId);
				if (term == null) throw new CustomException("Term not found.");
			}
			
			String username = dataObj.get("username").getAsString();
			//Checking if this combination of username and term exists
			if (UserManager.usernameExists(em, username, role, term, null)) {
				throw new CustomException("Username already exists for the selected term & role.");
			}
			String fullName = dataObj.get("fullName").getAsString();
			
		} catch (CustomException e) {
			json.put("success", false);
			json.put("message", e.getMessage());
		} catch (IllegalArgumentException e) {
			json.put("success", false);
			json.put("message", "Unknown role. Please specify the correct role.");
		} catch (Exception e) {
			
		}
	}
	
	//Method to edit an existing user in the system
	public void	editUser() {
		
	}
	
	//Method to delete an existing user from the system
	public void	deleteUser() {
		
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
	
	public class CustomException extends Exception {
		public CustomException(String string) {
			super(string);
		}
	}
	
}
